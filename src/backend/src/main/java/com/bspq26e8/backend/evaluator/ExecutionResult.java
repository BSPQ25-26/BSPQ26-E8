package com.bspq26e8.backend.evaluator;

import com.bspq26e8.backend.submission.entity.SubmissionStatus;

public record ExecutionResult(
        SubmissionStatus status,
        String verdictMessage,
        Integer runtimeMs,
        Integer memoryMb,
        int testcasesPassed,
        Integer testcasesTotal
) {
}
