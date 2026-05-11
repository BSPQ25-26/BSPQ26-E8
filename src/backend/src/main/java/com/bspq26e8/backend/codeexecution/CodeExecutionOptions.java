package com.bspq26e8.backend.codeexecution;

public record CodeExecutionOptions(
        boolean runSampleTests,
        boolean runHiddenTests,
        boolean runAdditionalVariations
) {

    public static CodeExecutionOptions standard() {
        return new CodeExecutionOptions(false, true, false);
    }
}
