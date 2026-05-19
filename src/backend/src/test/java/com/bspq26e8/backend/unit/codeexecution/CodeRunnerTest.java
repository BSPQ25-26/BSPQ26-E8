package com.bspq26e8.backend.unit.codeexecution;

import com.bspq26e8.backend.codeexecution.*;
import com.bspq26e8.backend.evaluator.RawExecutionResult;
import com.bspq26e8.backend.evaluator.RawTestCaseResult;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeRunnerTest {

    @Test
    void runReturnsRawFailureWhenJudge0IsDisabled() {
        Judge0Properties properties = judge0Properties(false);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of());
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        RawExecutionResult result = codeRunner.run(request());

        assertTrue(result.failed());
        assertEquals("Judge0 execution is disabled", result.errorMessage());
        assertEquals(2, result.testcasesTotal());
        assertTrue(judge0Client.createdSubmissions.isEmpty());
    }

    @Test
    void runSubmitsBatchAndReturnsRawTestCaseResults() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                result("token-1", 3, "Accepted", "hello\n", "0.042", 131072),
                result("token-2", 3, "Accepted", "bye\n", "0.011", 65536)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        RawExecutionResult result = codeRunner.run(request());

        assertFalse(result.failed());
        assertEquals(2, result.testcasesTotal());
        assertEquals(2, result.testCases().size());
        assertEquals(2, judge0Client.createdSubmissions.size());
        assertEquals(71, judge0Client.createdSubmissions.getFirst().languageId());
        assertNull(judge0Client.createdSubmissions.getFirst().expectedOutput());

        RawTestCaseResult first = result.testCases().getFirst();
        assertEquals(0, first.index());
        assertEquals("hello", first.inputData());
        assertEquals("hello", first.expectedOutput());
        assertEquals("hello\n", first.stdout());
        assertEquals(3, first.judge0StatusId());
    }

    @Test
    void runReturnsJudge0DiagnosticsAsRawData() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                resultWithDiagnostics("token-1", 6, "Compilation Error", "SyntaxError", null, null),
                result("token-2", 3, "Accepted", "bye\n", "0.020", 2048)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        RawExecutionResult result = codeRunner.run(request());

        RawTestCaseResult first = result.testCases().getFirst();
        assertEquals(6, first.judge0StatusId());
        assertEquals("Compilation Error", first.judge0StatusDescription());
        assertEquals("SyntaxError", first.compileOutput());
    }

    @Test
    void runReturnsUnfinishedRawResultWhenPollingLimitIsReached() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                result("token-1", 2, "Processing", null, null, null),
                result("token-2", 3, "Accepted", "bye\n", "0.020", 2048)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        RawExecutionResult result = codeRunner.run(request());

        assertFalse(result.testCases().getFirst().finished());
        assertEquals("Processing", result.testCases().getFirst().judge0StatusDescription());
    }

    private CodeExecutionRequest request() {
        return new CodeExecutionRequest(
                UUID.randomUUID(),
                "print(input())",
                new CodeExecutionRequest.LanguageSpec(1L, "python", "Python 3", null, "python3 main.py"),
                new CodeExecutionRequest.ProblemSpec(UUID.randomUUID(), null, "{}"),
                List.of(
                        new CodeExecutionRequest.TestCaseSpec("hello", "hello", true),
                        new CodeExecutionRequest.TestCaseSpec("bye", "bye", false)
                ),
                CodeExecutionOptions.standard()
        );
    }

    private Judge0Properties judge0Properties(boolean enabled) {
        Judge0Properties properties = new Judge0Properties();
        properties.setEnabled(enabled);
        properties.setPollInterval(Duration.ZERO);
        properties.setMaxPollAttempts(1);
        return properties;
    }

    private Judge0Client.Judge0SubmissionResult result(
            String token,
            int statusId,
            String statusDescription,
            String stdout,
            String time,
            Integer memoryKb
    ) {
        return new Judge0Client.Judge0SubmissionResult(
                token,
                statusId,
                statusDescription,
                stdout,
                null,
                null,
                null,
                time,
                memoryKb
        );
    }

    private Judge0Client.Judge0SubmissionResult resultWithDiagnostics(
            String token,
            int statusId,
            String statusDescription,
            String compileOutput,
            String stderr,
            String message
    ) {
        return new Judge0Client.Judge0SubmissionResult(
                token,
                statusId,
                statusDescription,
                null,
                stderr,
                compileOutput,
                message,
                "0.010",
                1024
        );
    }

    private static class FakeJudge0Client implements Judge0Client {

        private final List<Judge0SubmissionResult> results;
        private final List<Judge0SubmissionRequest> createdSubmissions = new ArrayList<>();

        private FakeJudge0Client(List<Judge0Client.Judge0SubmissionResult> results) {
            this.results = results;
        }

        @Override
        public List<Judge0SubmissionCreation> createBatch(List<Judge0SubmissionRequest> submissions) {
            createdSubmissions.addAll(submissions);
            List<Judge0SubmissionCreation> creations = new ArrayList<>();
            for (int index = 0; index < submissions.size(); index++) {
                creations.add(new Judge0SubmissionCreation("token-" + (index + 1), null));
            }
            return creations;
        }

        @Override
        public List<Judge0SubmissionResult> getBatch(List<String> tokens) {
            return results;
        }
    }
}
