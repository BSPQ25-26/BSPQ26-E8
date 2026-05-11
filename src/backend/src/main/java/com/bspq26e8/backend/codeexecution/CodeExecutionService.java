package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.problem.entity.TestCase;
import com.bspq26e8.backend.submission.entity.Submission;
import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import com.bspq26e8.backend.submission.repository.SubmissionRepository;
import com.bspq26e8.backend.submission.service.SubmissionService;
import com.bspq26e8.backend.submission.service.SubmissionService.ApplyExecutionResultCommand;
import com.bspq26e8.backend.submission.service.SubmissionService.ApplyExecutionResultResult;
import com.bspq26e8.backend.submission.service.SubmissionService.StartExecutionResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionService submissionService;
    private final CodeRunner codeRunner;

    public CodeExecutionService(
            SubmissionRepository submissionRepository,
            SubmissionService submissionService,
            CodeRunner codeRunner
    ) {
        this.submissionRepository = submissionRepository;
        this.submissionService = submissionService;
        this.codeRunner = codeRunner;
    }

    public QueueProcessingResult processQueuedSubmissions(int limit) {
        int safeLimit = Math.max(1, limit);
        List<ProcessSubmissionResult> results = submissionRepository.findQueuedIds(safeLimit)
                .stream()
                .map(this::processSubmission)
                .toList();

        long processed = results.stream().filter(ProcessSubmissionResult::processed).count();
        long failed = results.stream().filter(ProcessSubmissionResult::failed).count();
        long skipped = results.stream().filter(ProcessSubmissionResult::skipped).count();

        return new QueueProcessingResult(results.size(), processed, failed, skipped, results);
    }

    public ProcessSubmissionResult processSubmission(UUID submissionId) {
        Optional<Submission> maybeSubmission = submissionRepository.findByIdWithExecutionData(submissionId);
        if (maybeSubmission.isEmpty()) {
            return ProcessSubmissionResult.failed(submissionId, "Submission not found");
        }

        Submission submission = maybeSubmission.get();
        if (submission.getStatus() != SubmissionStatus.QUEUED) {
            return ProcessSubmissionResult.skipped(submissionId, "Submission is not queued");
        }

        CodeExecutionRequest request = toRequest(submission);
        StartExecutionResult startResult = submissionService.markExecutionStarted(submissionId);
        if (!startResult.started()) {
            return ProcessSubmissionResult.skipped(submissionId, startResult.errorMessage());
        }

        try {
            ExecutionResult executionResult = codeRunner.run(request);
            applyExecutionResult(submissionId, executionResult);
            return ProcessSubmissionResult.processed(submissionId);
        } catch (RuntimeException ex) {
            ExecutionResult failedResult = new ExecutionResult(
                    SubmissionStatus.INTERNAL_ERROR,
                    "Code execution failed: " + ex.getMessage(),
                    null,
                    null,
                    0,
                    request.testCases().size()
            );
            applyExecutionResult(submissionId, failedResult);
            return ProcessSubmissionResult.failed(submissionId, failedResult.verdictMessage());
        }
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

    private CodeExecutionRequest toRequest(Submission submission) {
        List<CodeExecutionRequest.TestCaseSpec> testCases = submission.getProblem().getTestCases() == null
                ? List.of()
                : submission.getProblem().getTestCases().stream()
                        .map(this::toTestCaseSpec)
                        .toList();

        return new CodeExecutionRequest(
                submission.getId(),
                submission.getSourceCode(),
                new CodeExecutionRequest.LanguageSpec(
                        submission.getLanguage().getId(),
                        submission.getLanguage().getCode(),
                        submission.getLanguage().getName(),
                        submission.getLanguage().getCompileCmd(),
                        submission.getLanguage().getRunCmd()
                ),
                new CodeExecutionRequest.ProblemSpec(
                        submission.getProblem().getId(),
                        submission.getProblem().getSolutionTemplate(),
                        submission.getProblem().getLanguageCompilationConfig()
                ),
                testCases,
                CodeExecutionOptions.standard()
        );
    }

    private CodeExecutionRequest.TestCaseSpec toTestCaseSpec(TestCase testCase) {
        return new CodeExecutionRequest.TestCaseSpec(
                testCase.getInputData(),
                testCase.getExpectedOutput(),
                testCase.isSample()
        );
    }

    public record QueueProcessingResult(
            int total,
            long processed,
            long failed,
            long skipped,
            List<ProcessSubmissionResult> submissions
    ) {
    }

    public record ProcessSubmissionResult(
            UUID submissionId,
            boolean processed,
            boolean failed,
            boolean skipped,
            String message
    ) {

        public static ProcessSubmissionResult processed(UUID submissionId) {
            return new ProcessSubmissionResult(submissionId, true, false, false, null);
        }

        public static ProcessSubmissionResult failed(UUID submissionId, String message) {
            return new ProcessSubmissionResult(submissionId, false, true, false, message);
        }

        public static ProcessSubmissionResult skipped(UUID submissionId, String message) {
            return new ProcessSubmissionResult(submissionId, false, false, true, message);
        }
    }
}
