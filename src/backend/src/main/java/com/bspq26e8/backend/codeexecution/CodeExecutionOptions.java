package com.bspq26e8.backend.codeexecution;

public record CodeExecutionOptions(
        boolean runSampleTests,
        boolean runHiddenTests
) {

    public static CodeExecutionOptions standard() {
        return new CodeExecutionOptions(true, true);
    }

    public static CodeExecutionOptions sampleOnly() {
        return new CodeExecutionOptions(true, false);
    }
}
