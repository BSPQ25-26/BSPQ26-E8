package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import org.springframework.stereotype.Component;

@Component
public class CodeRunner {

    public ExecutionResult run(CodeExecutionRequest request) {
        return new ExecutionResult(
                SubmissionStatus.INTERNAL_ERROR,
                "Code runner is not configured yet",
                null,
                null,
                0,
                request.testCases().size()
        );
    }
}
