package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.evaluator.ExecutionResult;
import com.bspq26e8.backend.evaluator.RawExecutionResult;
import com.bspq26e8.backend.evaluator.RawTestCaseResult;
import com.bspq26e8.backend.evaluator.SubmissionEvaluator;
import com.bspq26e8.backend.problem.entity.Language;
import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.problem.entity.TestCase;
import com.bspq26e8.backend.problem.repository.LanguageRepository;
import com.bspq26e8.backend.problem.repository.ProblemRepository;
import com.bspq26e8.backend.submission.entity.SubmissionStatus;
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
class CodeExecutionPreviewServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private CodeRunner codeRunner;

    @Mock
    private SubmissionEvaluator submissionEvaluator;

    @InjectMocks
    private CodeExecutionPreviewService service;

    @Test
    void runPreviewExecutesOnlySampleTestCasesAndReturnsEvaluatedRawJudge0Results() {
        UUID problemId = UUID.randomUUID();
        Problem problem = org.mockito.Mockito.mock(Problem.class);
        Language language = org.mockito.Mockito.mock(Language.class);
        TestCase sample = new TestCase(problem, "hello", "hello\n", true);
        TestCase hidden = new TestCase(problem, "secret", "secret\n", false);
        RawExecutionResult rawResult = RawExecutionResult.completed(List.of(
                new RawTestCaseResult(
                        0,
                        "hello",
                        "hello\n",
                        "hello\n",
                        null,
                        null,
                        null,
                        3,
                        "Accepted",
                        "0.010",
                        1024
                )
        ));
        ExecutionResult evaluatedResult = new ExecutionResult(
                SubmissionStatus.ACCEPTED,
                "Accepted",
                10,
                1,
                1,
                1
        );

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(languageRepository.findByCodeIgnoreCase("python")).thenReturn(Optional.of(language));
        when(problem.getId()).thenReturn(problemId);
        when(problem.getSolutionTemplate()).thenReturn(null);
        when(problem.getLanguageCompilationConfig()).thenReturn("{}");
        when(problem.getTestCases()).thenReturn(List.of(sample, hidden));
        when(language.getId()).thenReturn(1L);
        when(language.getCode()).thenReturn("python");
        when(language.getName()).thenReturn("Python 3");
        when(language.getCompileCmd()).thenReturn(null);
        when(language.getRunCmd()).thenReturn("python3 main.py");
        when(codeRunner.run(any(CodeExecutionRequest.class))).thenReturn(rawResult);
        when(submissionEvaluator.evaluate(rawResult)).thenReturn(evaluatedResult);

        CodeExecutionPreviewService.PreviewExecutionResult result = service.runPreview(
                new CodeExecutionPreviewService.PreviewExecutionCommand(
                        problemId,
                        null,
                        null,
                        "python",
                        "print(input())"
                )
        );

        assertTrue(result.executed());
        assertFalse(result.execution().failed());
        assertEquals(SubmissionStatus.ACCEPTED, result.execution().status());
        assertEquals("Accepted", result.execution().verdictMessage());
        assertEquals(10, result.execution().runtimeMs());
        assertEquals(1, result.execution().memoryMb());
        assertEquals(1, result.execution().testcasesPassed());
        assertEquals(1, result.execution().testcasesTotal());
        assertEquals("hello\n", result.execution().testCases().getFirst().stdout());
        assertEquals("Accepted", result.execution().testCases().getFirst().judge0StatusDescription());

        ArgumentCaptor<CodeExecutionRequest> requestCaptor = ArgumentCaptor.forClass(CodeExecutionRequest.class);
        verify(codeRunner).run(requestCaptor.capture());
        CodeExecutionRequest request = requestCaptor.getValue();
        assertEquals("print(input())", request.sourceCode());
        assertEquals("python", request.language().code());
        assertTrue(request.options().runSampleTests());
        assertFalse(request.options().runHiddenTests());
        assertEquals(1, request.testCases().size());
        assertEquals("hello", request.testCases().getFirst().inputData());
        verify(submissionEvaluator).evaluate(rawResult);
    }

    @Test
    void runPreviewReturnsNotFoundWhenProblemDoesNotExist() {
        UUID problemId = UUID.randomUUID();
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        CodeExecutionPreviewService.PreviewExecutionResult result = service.runPreview(
                new CodeExecutionPreviewService.PreviewExecutionCommand(
                        problemId,
                        1L,
                        null,
                        null,
                        "print('hello')"
                )
        );

        assertFalse(result.executed());
        assertTrue(result.notFound());
        assertEquals("Problem not found", result.errorMessage());
    }

    @Test
    void runPreviewReturnsNotFoundWhenLanguageDoesNotExist() {
        UUID problemId = UUID.randomUUID();
        Problem problem = org.mockito.Mockito.mock(Problem.class);

        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(languageRepository.findById(99L)).thenReturn(Optional.empty());

        CodeExecutionPreviewService.PreviewExecutionResult result = service.runPreview(
                new CodeExecutionPreviewService.PreviewExecutionCommand(
                        problemId,
                        99L,
                        null,
                        null,
                        "print('hello')"
                )
        );

        assertFalse(result.executed());
        assertTrue(result.notFound());
        assertEquals("Language not found", result.errorMessage());
    }
}
