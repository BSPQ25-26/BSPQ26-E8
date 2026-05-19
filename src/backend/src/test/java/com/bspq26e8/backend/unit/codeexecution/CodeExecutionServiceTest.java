package com.bspq26e8.backend.unit.codeexecution;

import com.bspq26e8.backend.codeexecution.CodeExecutionRequest;
import com.bspq26e8.backend.codeexecution.CodeExecutionService;
import com.bspq26e8.backend.codeexecution.CodeRunner;
import com.bspq26e8.backend.evaluator.ExecutionResult;
import com.bspq26e8.backend.evaluator.RawExecutionResult;
import com.bspq26e8.backend.evaluator.SubmissionEvaluator;
import com.bspq26e8.backend.problem.entity.Language;
import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.submission.entity.Submission;
import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import com.bspq26e8.backend.submission.repository.SubmissionRepository;
import com.bspq26e8.backend.submission.service.SubmissionService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeExecutionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private CodeRunner codeRunner;

    @Mock
    private SubmissionEvaluator submissionEvaluator;

    @InjectMocks
    private CodeExecutionService codeExecutionService;

    @Test
    void processSubmissionSkipsWhenSubmissionIsNotQueued() {
        UUID submissionId = UUID.randomUUID();
        Submission submission = org.mockito.Mockito.mock(Submission.class);

        when(submissionRepository.findByIdWithExecutionData(submissionId)).thenReturn(Optional.of(submission));
        when(submission.getStatus()).thenReturn(SubmissionStatus.ACCEPTED);

        CodeExecutionService.ProcessSubmissionResult result = codeExecutionService.processSubmission(submissionId);

        assertFalse(result.processed());
        assertTrue(result.skipped());
        assertEquals("Submission is not queued", result.message());
    }

    @Test
    void processSubmissionRunsQueuedSubmissionAndAppliesResult() {
        UUID submissionId = UUID.randomUUID();
        Submission submission = queuedSubmission(submissionId);
        ExecutionResult executionResult = new ExecutionResult(
                SubmissionStatus.ACCEPTED,
                "Accepted",
                42,
                128,
                1,
                1
        );
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of());

        when(submissionRepository.findByIdWithExecutionData(submissionId)).thenReturn(Optional.of(submission));
        when(submissionService.markExecutionStarted(submissionId))
                .thenReturn(SubmissionService.StartExecutionResult.started(null));
        when(codeRunner.run(any(CodeExecutionRequest.class))).thenReturn(rawResult);
        when(submissionEvaluator.evaluate(rawResult)).thenReturn(executionResult);
        when(submissionService.applyExecutionResult(any(SubmissionService.ApplyExecutionResultCommand.class)))
                .thenReturn(SubmissionService.ApplyExecutionResultResult.updated(null));

        CodeExecutionService.ProcessSubmissionResult result = codeExecutionService.processSubmission(submissionId);

        assertTrue(result.processed());
        assertFalse(result.failed());

        ArgumentCaptor<CodeExecutionRequest> requestCaptor = ArgumentCaptor.forClass(CodeExecutionRequest.class);
        verify(codeRunner).run(requestCaptor.capture());
        assertEquals(submissionId, requestCaptor.getValue().submissionId());
        assertEquals("print('hello')", requestCaptor.getValue().sourceCode());
        assertEquals("python", requestCaptor.getValue().language().code());
        verify(submissionEvaluator).evaluate(rawResult);

        ArgumentCaptor<SubmissionService.ApplyExecutionResultCommand> resultCaptor =
                ArgumentCaptor.forClass(SubmissionService.ApplyExecutionResultCommand.class);
        verify(submissionService).applyExecutionResult(resultCaptor.capture());
        assertEquals(submissionId, resultCaptor.getValue().submissionId());
        assertEquals(SubmissionStatus.ACCEPTED, resultCaptor.getValue().status());
    }

    @Test
    void processQueuedSubmissionsProcessesQueuedIds() {
        UUID firstSubmissionId = UUID.randomUUID();
        UUID secondSubmissionId = UUID.randomUUID();
        Submission firstSubmission = queuedSubmission(firstSubmissionId);

        when(submissionRepository.findQueuedIds(2)).thenReturn(List.of(firstSubmissionId, secondSubmissionId));
        when(submissionRepository.findByIdWithExecutionData(firstSubmissionId))
                .thenReturn(Optional.of(firstSubmission));
        when(submissionRepository.findByIdWithExecutionData(secondSubmissionId))
                .thenReturn(Optional.empty());
        when(submissionService.markExecutionStarted(firstSubmissionId))
                .thenReturn(SubmissionService.StartExecutionResult.started(null));
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of());
        when(codeRunner.run(any(CodeExecutionRequest.class))).thenReturn(rawResult);
        when(submissionEvaluator.evaluate(rawResult)).thenReturn(new ExecutionResult(
                SubmissionStatus.ACCEPTED,
                "Accepted",
                42,
                128,
                1,
                1
        ));
        when(submissionService.applyExecutionResult(any(SubmissionService.ApplyExecutionResultCommand.class)))
                .thenReturn(SubmissionService.ApplyExecutionResultResult.updated(null));

        CodeExecutionService.QueueProcessingResult result = codeExecutionService.processQueuedSubmissions(2);

        assertEquals(2, result.total());
        assertEquals(1, result.processed());
        assertEquals(1, result.failed());
    }

    private Submission queuedSubmission(UUID submissionId) {
        Submission submission = org.mockito.Mockito.mock(Submission.class);
        Problem problem = org.mockito.Mockito.mock(Problem.class);
        Language language = org.mockito.Mockito.mock(Language.class);

        when(submission.getId()).thenReturn(submissionId);
        when(submission.getStatus()).thenReturn(SubmissionStatus.QUEUED);
        when(submission.getSourceCode()).thenReturn("print('hello')");
        when(submission.getProblem()).thenReturn(problem);
        when(submission.getLanguage()).thenReturn(language);

        when(problem.getId()).thenReturn(UUID.randomUUID());
        when(problem.getSolutionTemplate()).thenReturn(null);
        when(problem.getLanguageCompilationConfig()).thenReturn("{}");
        when(problem.getTestCases()).thenReturn(List.of());

        when(language.getId()).thenReturn(1L);
        when(language.getCode()).thenReturn("python");
        when(language.getName()).thenReturn("Python");
        when(language.getCompileCmd()).thenReturn(null);
        when(language.getRunCmd()).thenReturn("python main.py");

        return submission;
    }
}
