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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeExecutionPreviewService {

    private final ProblemRepository problemRepository;
    private final LanguageRepository languageRepository;
    private final CodeRunner codeRunner;
    private final SubmissionEvaluator submissionEvaluator;

    public CodeExecutionPreviewService(
            ProblemRepository problemRepository,
            LanguageRepository languageRepository,
            CodeRunner codeRunner,
            SubmissionEvaluator submissionEvaluator
    ) {
        this.problemRepository = problemRepository;
        this.languageRepository = languageRepository;
        this.codeRunner = codeRunner;
        this.submissionEvaluator = submissionEvaluator;
    }

    @Transactional(readOnly = true)
    public PreviewExecutionResult runPreview(PreviewExecutionCommand command) {
        Optional<Problem> maybeProblem = problemRepository.findById(command.problemId());
        if (maybeProblem.isEmpty()) {
            return PreviewExecutionResult.notFound("Problem not found");
        }

        Optional<Language> maybeLanguage = findLanguage(command);
        if (maybeLanguage.isEmpty()) {
            return PreviewExecutionResult.notFound("Language not found");
        }

        Problem problem = maybeProblem.get();
        Language language = maybeLanguage.get();
        List<CodeExecutionRequest.TestCaseSpec> sampleTestCases = sampleTestCases(problem);

        CodeExecutionRequest request = new CodeExecutionRequest(
                null,
                command.sourceCode(),
                new CodeExecutionRequest.LanguageSpec(
                        language.getId(),
                        language.getCode(),
                        language.getName(),
                        language.getCompileCmd(),
                        language.getRunCmd()
                ),
                new CodeExecutionRequest.ProblemSpec(
                        problem.getId(),
                        problem.getSolutionTemplate(),
                        problem.getLanguageCompilationConfig()
                ),
                sampleTestCases,
                CodeExecutionOptions.sampleOnly()
        );

        RawExecutionResult rawResult = codeRunner.run(request);
        ExecutionResult evaluation = submissionEvaluator.evaluate(rawResult);
        return PreviewExecutionResult.executed(toView(rawResult, evaluation));
    }

    private Optional<Language> findLanguage(PreviewExecutionCommand command) {
        if (command.languageId() != null) {
            return languageRepository.findById(command.languageId());
        }

        String languageCode = firstNonBlank(command.languageCode(), command.language());
        if (languageCode == null) {
            return Optional.empty();
        }

        return languageRepository.findByCodeIgnoreCase(languageCode);
    }

    private List<CodeExecutionRequest.TestCaseSpec> sampleTestCases(Problem problem) {
        if (problem.getTestCases() == null) {
            return List.of();
        }

        return problem.getTestCases().stream()
                .filter(TestCase::isSample)
                .map(testCase -> new CodeExecutionRequest.TestCaseSpec(
                        testCase.getInputData(),
                        testCase.getExpectedOutput(),
                        true
                ))
                .toList();
    }

    private PreviewExecutionView toView(RawExecutionResult result, ExecutionResult evaluation) {
        return new PreviewExecutionView(
                result.failed(),
                result.errorMessage(),
                evaluation.status(),
                evaluation.verdictMessage(),
                evaluation.runtimeMs(),
                evaluation.memoryMb(),
                evaluation.testcasesPassed(),
                result.testcasesTotal(),
                result.testCases().stream()
                        .map(this::toTestCaseView)
                        .toList()
        );
    }

    private PreviewTestCaseView toTestCaseView(RawTestCaseResult testCase) {
        return new PreviewTestCaseView(
                testCase.index(),
                testCase.inputData(),
                testCase.expectedOutput(),
                testCase.stdout(),
                testCase.stderr(),
                testCase.compileOutput(),
                testCase.message(),
                testCase.judge0StatusId(),
                testCase.judge0StatusDescription(),
                testCase.time(),
                testCase.memoryKb()
        );
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        if (!isBlank(second)) {
            return second;
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record PreviewExecutionCommand(
            UUID problemId,
            Long languageId,
            String languageCode,
            String language,
            String sourceCode
    ) {
    }

    public record PreviewExecutionResult(
            boolean executed,
            boolean notFound,
            String errorMessage,
            PreviewExecutionView execution
    ) {

        public static PreviewExecutionResult executed(PreviewExecutionView execution) {
            return new PreviewExecutionResult(true, false, null, execution);
        }

        public static PreviewExecutionResult notFound(String errorMessage) {
            return new PreviewExecutionResult(false, true, errorMessage, null);
        }
    }

    public record PreviewExecutionView(
            boolean failed,
            String errorMessage,
            SubmissionStatus status,
            String verdictMessage,
            Integer runtimeMs,
            Integer memoryMb,
            int testcasesPassed,
            int testcasesTotal,
            List<PreviewTestCaseView> testCases
    ) {
    }

    public record PreviewTestCaseView(
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
    }
}
