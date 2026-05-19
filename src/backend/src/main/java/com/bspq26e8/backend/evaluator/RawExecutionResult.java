package com.bspq26e8.backend.evaluator;

import java.util.List;

public record RawExecutionResult(
        List<RawTestCaseResult> testCases,
        String errorMessage,
        int testcasesTotal
) {

    public RawExecutionResult {
        testCases = testCases == null ? List.of() : List.copyOf(testCases);
    }

    public static RawExecutionResult completed(List<RawTestCaseResult> testCases) {
        return new RawExecutionResult(testCases, null, testCases == null ? 0 : testCases.size());
    }

    public static RawExecutionResult failed(String errorMessage, int testcasesTotal) {
        return new RawExecutionResult(List.of(), errorMessage, testcasesTotal);
    }

    public boolean failed() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
