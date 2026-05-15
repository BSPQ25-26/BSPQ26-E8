package com.bspq26e8.backend.evaluator;

import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SubmissionEvaluatorTest {

    private final SubmissionEvaluator evaluator = new SubmissionEvaluator();

    @Test
    void evaluateAcceptsWhenAllOutputsMatch() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                result(0, "hello\n", "hello", 3, "Accepted", "0.042", 131072),
                result(1, "bye\r\n", "bye\n", 3, "Accepted", "0.011", 65536)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.ACCEPTED, result.status());
        assertEquals("Accepted", result.verdictMessage());
        assertEquals(42, result.runtimeMs());
        assertEquals(128, result.memoryMb());
        assertEquals(2, result.testcasesPassed());
        assertEquals(2, result.testcasesTotal());
    }

    @Test
    void evaluateMarksWrongAnswerWhenJudge0AcceptedButOutputDoesNotMatch() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                result(0, "actual", "expected", 3, "Accepted", "0.010", 1024)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.WRONG_ANSWER, result.status());
        assertEquals("Wrong Answer", result.verdictMessage());
        assertEquals(0, result.testcasesPassed());
        assertEquals(1, result.testcasesTotal());
    }

    @Test
    void evaluatePrioritizesCompileErrorAndUsesDiagnostics() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                resultWithDiagnostics(0, null, "expected", 6, "Compilation Error", "SyntaxError", null, null),
                result(1, "bye", "bye", 3, "Accepted", "0.020", 2048)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.COMPILE_ERROR, result.status());
        assertEquals("SyntaxError", result.verdictMessage());
        assertEquals(1, result.testcasesPassed());
        assertEquals(2, result.testcasesTotal());
    }

    @Test
    void evaluateMapsRuntimeError() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                resultWithDiagnostics(0, null, "expected", 11, "Runtime Error (NZEC)", null, "Traceback", null)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.RUNTIME_ERROR, result.status());
        assertEquals("Traceback", result.verdictMessage());
    }

    @Test
    void evaluateMapsTimeLimitExceeded() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                result(0, null, "expected", 5, "Time Limit Exceeded", "5.000", 1024)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.TIME_LIMIT_EXCEEDED, result.status());
        assertEquals("Time Limit Exceeded", result.verdictMessage());
    }

    @Test
    void evaluateReturnsInternalErrorWhenJudge0DidNotFinish() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                result(0, null, "expected", 2, "Processing", null, null)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.INTERNAL_ERROR, result.status());
        assertEquals("Judge0 execution did not finish before polling limit", result.verdictMessage());
    }

    @Test
    void evaluateDoesNotReturnZeroMetricsBecauseSubmissionTableRequiresPositiveValues() {
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                result(0, "hello", "hello", 3, "Accepted", "0.000", 0)
        ));

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.ACCEPTED, result.status());
        assertNull(result.runtimeMs());
        assertNull(result.memoryMb());
    }

    @Test
    void evaluateReturnsInternalErrorForRawExecutionFailure() {
        RawExecutionResult rawResult = RawExecutionResult.failed("Judge0 rejected submission", 2);

        ExecutionResult result = evaluator.evaluate(rawResult);

        assertEquals(SubmissionStatus.INTERNAL_ERROR, result.status());
        assertEquals("Judge0 rejected submission", result.verdictMessage());
        assertEquals(2, result.testcasesTotal());
    }

    private RawTestCaseResult result(
            int index,
            String stdout,
            String expectedOutput,
            int statusId,
            String statusDescription,
            String time,
            Integer memoryKb
    ) {
        return new RawTestCaseResult(
                index,
                "input",
                expectedOutput,
                stdout,
                null,
                null,
                null,
                statusId,
                statusDescription,
                time,
                memoryKb
        );
    }

    private RawTestCaseResult resultWithDiagnostics(
            int index,
            String stdout,
            String expectedOutput,
            int statusId,
            String statusDescription,
            String compileOutput,
            String stderr,
            String message
    ) {
        return new RawTestCaseResult(
                index,
                "input",
                expectedOutput,
                stdout,
                stderr,
                compileOutput,
                message,
                statusId,
                statusDescription,
                "0.010",
                1024
        );
    }
}
