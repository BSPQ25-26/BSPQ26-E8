package com.bspq26e8.backend.evaluator;

public record RawTestCaseResult(
        int index,
        String inputData,
        String expectedOutput,
        String stdout,
        String stderr,
        String compileOutput,
        String message,
        Integer judge0StatusId,
        String judge0StatusDescription,
        String time,
        Integer memoryKb
) {

    public boolean finished() {
        return judge0StatusId != null && judge0StatusId > 2;
    }
}
