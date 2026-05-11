package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.submission.service.SubmissionService;
import com.bspq26e8.backend.submission.service.SubmissionService.ApplyExecutionResultCommand;
import com.bspq26e8.backend.submission.service.SubmissionService.ApplyExecutionResultResult;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

    private final SubmissionService submissionService;

    public CodeExecutionService(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    public ApplyExecutionResultResult applyExecutionResult(UUID submissionId, ExecutionResult result) {
        if (result == null) {
            throw new IllegalArgumentException("result is required");
        }

        ApplyExecutionResultCommand command = new ApplyExecutionResultCommand(
                submissionId,
                result.status(),
                result.verdictMessage(),
                result.runtimeMs(),
                result.memoryMb(),
                result.testcasesPassed(),
                result.testcasesTotal()
        );

        return submissionService.applyExecutionResult(command);
    }
}
