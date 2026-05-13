package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeRunnerTest {

    @Test
    void runReturnsInternalErrorWhenJudge0IsDisabled() {
        Judge0Properties properties = judge0Properties(false);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of());
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        ExecutionResult result = codeRunner.run(request());

        assertEquals(SubmissionStatus.INTERNAL_ERROR, result.status());
        assertEquals("Judge0 execution is disabled", result.verdictMessage());
        assertTrue(judge0Client.createdSubmissions.isEmpty());
    }

    @Test
    void runSubmitsBatchAndAggregatesAcceptedResults() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                result("token-1", 3, "Accepted", "0.042", 131072),
                result("token-2", 3, "Accepted", "0.011", 65536)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        ExecutionResult result = codeRunner.run(request());

        assertEquals(SubmissionStatus.ACCEPTED, result.status());
        assertEquals("Accepted", result.verdictMessage());
        assertEquals(42, result.runtimeMs());
        assertEquals(128, result.memoryMb());
        assertEquals(2, result.testcasesPassed());
        assertEquals(2, result.testcasesTotal());
        assertEquals(2, judge0Client.createdSubmissions.size());
        assertEquals(71, judge0Client.createdSubmissions.getFirst().languageId());
    }

    @Test
    void runAggregatesWrongAnswerWhenAnyCaseFails() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                result("token-1", 3, "Accepted", "0.010", 1024),
                result("token-2", 4, "Wrong Answer", "0.020", 2048)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        ExecutionResult result = codeRunner.run(request());

        assertEquals(SubmissionStatus.WRONG_ANSWER, result.status());
        assertEquals("Wrong Answer", result.verdictMessage());
        assertEquals(1, result.testcasesPassed());
        assertEquals(2, result.testcasesTotal());
    }

    @Test
    void runUsesExecutionDiagnosticAsVerdictMessage() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                resultWithDiagnostics("token-1", 6, "Compilation Error", "SyntaxError", null, null),
                result("token-2", 3, "Accepted", "0.020", 2048)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        ExecutionResult result = codeRunner.run(request());

        assertEquals(SubmissionStatus.COMPILE_ERROR, result.status());
        assertEquals("SyntaxError", result.verdictMessage());
        assertEquals(1, result.testcasesPassed());
        assertEquals(2, result.testcasesTotal());
    }

    @Test
    void runDoesNotReturnZeroMetricsBecauseSubmissionTableRequiresPositiveValues() {
        Judge0Properties properties = judge0Properties(true);
        FakeJudge0Client judge0Client = new FakeJudge0Client(List.of(
                result("token-1", 3, "Accepted", "0.000", 0),
                result("token-2", 3, "Accepted", "0.000", 0)
        ));
        CodeRunner codeRunner = new CodeRunner(judge0Client, properties);

        ExecutionResult result = codeRunner.run(request());

        assertEquals(SubmissionStatus.ACCEPTED, result.status());
        assertNull(result.runtimeMs());
        assertNull(result.memoryMb());
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
            String time,
            int memoryKb
    ) {
        return new Judge0Client.Judge0SubmissionResult(
                token,
                statusId,
                statusDescription,
                null,
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

        private FakeJudge0Client(List<Judge0SubmissionResult> results) {
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
