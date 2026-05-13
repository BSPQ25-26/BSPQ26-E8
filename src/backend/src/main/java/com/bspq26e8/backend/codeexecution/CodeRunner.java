package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    public ExecutionResult run(CodeExecutionRequest request) {
        if (!judge0Properties.isEnabled()) {
            return internalError("Judge0 execution is disabled", request.testCases().size());
        }

        Integer judge0LanguageId = judge0Properties.languageIdFor(request.language().code());
        if (judge0LanguageId == null) {
            return internalError("Judge0 language is not configured for " + request.language().code(), request.testCases().size());
        }

        List<CodeExecutionRequest.TestCaseSpec> selectedTestCases = selectTestCases(request);
        if (selectedTestCases.isEmpty()) {
            return internalError("No test cases selected for execution", request.testCases().size());
        }

        List<Judge0Client.Judge0SubmissionRequest> submissions = selectedTestCases.stream()
                .map(testCase -> new Judge0Client.Judge0SubmissionRequest(
                        judge0LanguageId,
                        request.sourceCode(),
                        testCase.inputData(),
                        testCase.expectedOutput()
                ))
                .toList();

        List<Judge0Client.Judge0SubmissionCreation> creations = judge0Client.createBatch(submissions);
        if (creations.size() != submissions.size()) {
            return internalError("Judge0 returned an unexpected number of submission tokens", selectedTestCases.size());
        }

        List<String> tokenCreationErrors = creations.stream()
                .filter(creation -> !creation.created())
                .map(Judge0Client.Judge0SubmissionCreation::errorMessage)
                .map(message -> isBlank(message) ? "Unknown Judge0 submission creation error" : message)
                .toList();
        if (!tokenCreationErrors.isEmpty()) {
            return internalError("Judge0 rejected submission: " + String.join("; ", tokenCreationErrors), selectedTestCases.size());
        }

        List<String> tokens = creations.stream()
                .map(Judge0Client.Judge0SubmissionCreation::token)
                .toList();

        List<Judge0Client.Judge0SubmissionResult> results = pollUntilFinished(tokens);
        if (results.stream().anyMatch(result -> !result.finished())) {
            return new ExecutionResult(
                    SubmissionStatus.INTERNAL_ERROR,
                    "Judge0 execution did not finish before polling limit",
                    null,
                    null,
                    countAccepted(results),
                    selectedTestCases.size()
            );
        }

        return aggregateResults(results, selectedTestCases.size());
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

    private void sleepBeforeNextPoll() {
        try {
            Thread.sleep(judge0Properties.getPollInterval().toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling Judge0", ex);
        }
    }

    private ExecutionResult aggregateResults(List<Judge0Client.Judge0SubmissionResult> results, int testcasesTotal) {
        int testcasesPassed = countAccepted(results);
        SubmissionStatus status = results.stream()
                .map(this::toSubmissionStatus)
                .filter(candidate -> candidate != SubmissionStatus.ACCEPTED)
                .findFirst()
                .orElse(SubmissionStatus.ACCEPTED);

        return new ExecutionResult(
                status,
                verdictMessage(status, results),
                runtimeMs(results),
                memoryMb(results),
                testcasesPassed,
                testcasesTotal
        );
    }

    private int countAccepted(List<Judge0Client.Judge0SubmissionResult> results) {
        return (int) results.stream()
                .filter(result -> toSubmissionStatus(result) == SubmissionStatus.ACCEPTED)
                .count();
    }

    private SubmissionStatus toSubmissionStatus(Judge0Client.Judge0SubmissionResult result) {
        return switch (result.statusId() == null ? -1 : result.statusId()) {
            case 3 -> SubmissionStatus.ACCEPTED;
            case 4 -> SubmissionStatus.WRONG_ANSWER;
            case 5 -> SubmissionStatus.TIME_LIMIT_EXCEEDED;
            case 6 -> SubmissionStatus.COMPILE_ERROR;
            case 7, 8, 9, 10, 11, 12, 14 -> SubmissionStatus.RUNTIME_ERROR;
            case 13 -> SubmissionStatus.INTERNAL_ERROR;
            default -> SubmissionStatus.INTERNAL_ERROR;
        };
    }

    private String verdictMessage(SubmissionStatus status, List<Judge0Client.Judge0SubmissionResult> results) {
        return results.stream()
                .filter(result -> toSubmissionStatus(result) == status)
                .findFirst()
                .map(this::messageFor)
                .orElse(status.name());
    }

    private String messageFor(Judge0Client.Judge0SubmissionResult result) {
        if (!isBlank(result.compileOutput())) {
            return result.compileOutput();
        }
        if (!isBlank(result.stderr())) {
            return result.stderr();
        }
        if (!isBlank(result.message())) {
            return result.message();
        }
        if (!isBlank(result.statusDescription())) {
            return result.statusDescription();
        }
        return "Execution finished";
    }

    private Integer runtimeMs(List<Judge0Client.Judge0SubmissionResult> results) {
        return results.stream()
                .map(Judge0Client.Judge0SubmissionResult::time)
                .filter(value -> !isBlank(value))
                .map(this::toMilliseconds)
                .filter(value -> value != null && value > 0)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private Integer memoryMb(List<Judge0Client.Judge0SubmissionResult> results) {
        return results.stream()
                .map(Judge0Client.Judge0SubmissionResult::memoryKb)
                .filter(value -> value != null && value > 0)
                .map(value -> (int) Math.ceil(value / 1024.0))
                .max(Integer::compareTo)
                .orElse(null);
    }

    private Integer toMilliseconds(String seconds) {
        try {
            return (int) Math.round(Double.parseDouble(seconds) * 1000);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private ExecutionResult internalError(String message, int testcasesTotal) {
        return new ExecutionResult(
                SubmissionStatus.INTERNAL_ERROR,
                message,
                null,
                null,
                0,
                testcasesTotal
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
