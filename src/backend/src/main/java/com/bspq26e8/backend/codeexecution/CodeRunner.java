package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.evaluator.RawExecutionResult;
import com.bspq26e8.backend.evaluator.RawTestCaseResult;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CodeRunner {

    private final Judge0Client judge0Client;
    private final Judge0Properties judge0Properties;

    public CodeRunner(Judge0Client judge0Client, Judge0Properties judge0Properties) {
        this.judge0Client = judge0Client;
        this.judge0Properties = judge0Properties;
    }

    public RawExecutionResult run(CodeExecutionRequest request) {
        if (!judge0Properties.isEnabled()) {
            return RawExecutionResult.failed("Judge0 execution is disabled", request.testCases().size());
        }

        Integer judge0LanguageId = judge0Properties.languageIdFor(request.language().code());
        if (judge0LanguageId == null) {
            return RawExecutionResult.failed(
                    "Judge0 language is not configured for " + request.language().code(),
                    request.testCases().size()
            );
        }

        List<CodeExecutionRequest.TestCaseSpec> selectedTestCases = selectTestCases(request);
        if (selectedTestCases.isEmpty()) {
            return RawExecutionResult.failed("No test cases selected for execution", request.testCases().size());
        }

        List<Judge0Client.Judge0SubmissionRequest> submissions = selectedTestCases.stream()
                .map(testCase -> new Judge0Client.Judge0SubmissionRequest(
                        judge0LanguageId,
                        request.sourceCode(),
                        testCase.inputData(),
                        null
                ))
                .toList();

        List<Judge0Client.Judge0SubmissionCreation> creations = judge0Client.createBatch(submissions);
        if (creations.size() != submissions.size()) {
            return RawExecutionResult.failed(
                    "Judge0 returned an unexpected number of submission tokens",
                    selectedTestCases.size()
            );
        }

        List<String> tokenCreationErrors = creations.stream()
                .filter(creation -> !creation.created())
                .map(Judge0Client.Judge0SubmissionCreation::errorMessage)
                .map(message -> isBlank(message) ? "Unknown Judge0 submission creation error" : message)
                .toList();
        if (!tokenCreationErrors.isEmpty()) {
            return RawExecutionResult.failed(
                    "Judge0 rejected submission: " + String.join("; ", tokenCreationErrors),
                    selectedTestCases.size()
            );
        }

        List<String> tokens = creations.stream()
                .map(Judge0Client.Judge0SubmissionCreation::token)
                .toList();

        List<Judge0Client.Judge0SubmissionResult> results = pollUntilFinished(tokens);
        return RawExecutionResult.completed(toRawTestCaseResults(selectedTestCases, tokens, results));
    }

    private List<CodeExecutionRequest.TestCaseSpec> selectTestCases(CodeExecutionRequest request) {
        CodeExecutionOptions options = request.options() == null ? CodeExecutionOptions.standard() : request.options();
        return request.testCases().stream()
                .filter(testCase -> (testCase.sample() && options.runSampleTests())
                        || (!testCase.sample() && options.runHiddenTests()))
                .toList();
    }

    private List<Judge0Client.Judge0SubmissionResult> pollUntilFinished(List<String> tokens) {
        List<Judge0Client.Judge0SubmissionResult> results = List.of();

        int maxPollAttempts = Math.max(1, judge0Properties.getMaxPollAttempts());
        for (int attempt = 0; attempt < maxPollAttempts; attempt++) {
            results = judge0Client.getBatch(tokens);
            if (results.size() == tokens.size() && results.stream().allMatch(Judge0Client.Judge0SubmissionResult::finished)) {
                return sortByTokenOrder(results, tokens);
            }
            sleepBeforeNextPoll();
        }

        return sortByTokenOrder(results, tokens);
    }

    private List<Judge0Client.Judge0SubmissionResult> sortByTokenOrder(
            List<Judge0Client.Judge0SubmissionResult> results,
            List<String> tokens
    ) {
        Map<String, Integer> orderByToken = tokens.stream()
                .collect(Collectors.toMap(Function.identity(), tokens::indexOf));

        return results.stream()
                .sorted(Comparator.comparingInt(result -> orderByToken.getOrDefault(result.token(), Integer.MAX_VALUE)))
                .toList();
    }

    private List<RawTestCaseResult> toRawTestCaseResults(
            List<CodeExecutionRequest.TestCaseSpec> selectedTestCases,
            List<String> tokens,
            List<Judge0Client.Judge0SubmissionResult> judge0Results
    ) {
        Map<String, Judge0Client.Judge0SubmissionResult> resultByToken = judge0Results.stream()
                .filter(result -> !isBlank(result.token()))
                .collect(Collectors.toMap(
                        Judge0Client.Judge0SubmissionResult::token,
                        Function.identity(),
                        (first, ignored) -> first
                ));

        return IntStream.range(0, selectedTestCases.size())
                .mapToObj(index -> toRawTestCaseResult(
                        index,
                        selectedTestCases.get(index),
                        resultByToken.get(tokens.get(index))
                ))
                .toList();
    }

    private RawTestCaseResult toRawTestCaseResult(
            int index,
            CodeExecutionRequest.TestCaseSpec testCase,
            Judge0Client.Judge0SubmissionResult result
    ) {
        if (result == null) {
            return new RawTestCaseResult(
                    index,
                    testCase.inputData(),
                    testCase.expectedOutput(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Missing Judge0 result",
                    null,
                    null
            );
        }

        return new RawTestCaseResult(
                index,
                testCase.inputData(),
                testCase.expectedOutput(),
                result.stdout(),
                result.stderr(),
                result.compileOutput(),
                result.message(),
                result.statusId(),
                result.statusDescription(),
                result.time(),
                result.memoryKb()
        );
    }

    private void sleepBeforeNextPoll() {
        try {
            Thread.sleep(judge0Properties.getPollInterval().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling Judge0", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
