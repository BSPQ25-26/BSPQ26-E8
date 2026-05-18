package com.bspq26e8.backend.evaluator;

import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SubmissionEvaluator {

    public ExecutionResult evaluate(RawExecutionResult rawResult) {
        if (rawResult == null) {
            return internalError("Code execution did not return a result", 0);
        }

        if (rawResult.failed()) {
            return internalError(rawResult.errorMessage(), rawResult.testcasesTotal());
        }

        if (rawResult.testCases().isEmpty()) {
            return internalError("Code execution did not return any test case results", rawResult.testcasesTotal());
        }

        int testcasesPassed = countPassed(rawResult.testCases());
        SubmissionStatus status = finalStatus(rawResult.testCases());

        return new ExecutionResult(
                status,
                verdictMessage(status, rawResult.testCases()),
                runtimeMs(rawResult.testCases()),
                memoryMb(rawResult.testCases()),
                testcasesPassed,
                rawResult.testcasesTotal()
        );
    }

    private SubmissionStatus finalStatus(List<RawTestCaseResult> results) {
        if (results.stream().anyMatch(result -> !result.finished())) {
            return SubmissionStatus.INTERNAL_ERROR;
        }
        if (results.stream().anyMatch(result -> toSubmissionStatus(result) == SubmissionStatus.COMPILE_ERROR)) {
            return SubmissionStatus.COMPILE_ERROR;
        }
        if (results.stream().anyMatch(result -> toSubmissionStatus(result) == SubmissionStatus.INTERNAL_ERROR)) {
            return SubmissionStatus.INTERNAL_ERROR;
        }
        if (results.stream().anyMatch(result -> toSubmissionStatus(result) == SubmissionStatus.TIME_LIMIT_EXCEEDED)) {
            return SubmissionStatus.TIME_LIMIT_EXCEEDED;
        }
        if (results.stream().anyMatch(result -> toSubmissionStatus(result) == SubmissionStatus.MEMORY_LIMIT_EXCEEDED)) {
            return SubmissionStatus.MEMORY_LIMIT_EXCEEDED;
        }
        if (results.stream().anyMatch(result -> toSubmissionStatus(result) == SubmissionStatus.RUNTIME_ERROR)) {
            return SubmissionStatus.RUNTIME_ERROR;
        }
        if (results.stream().anyMatch(result -> toSubmissionStatus(result) == SubmissionStatus.WRONG_ANSWER || !outputMatches(result))) {
            return SubmissionStatus.WRONG_ANSWER;
        }
        return SubmissionStatus.ACCEPTED;
    }

    private int countPassed(List<RawTestCaseResult> results) {
        return (int) results.stream()
                .filter(result -> toSubmissionStatus(result) == SubmissionStatus.ACCEPTED)
                .filter(this::outputMatches)
                .count();
    }

    private SubmissionStatus toSubmissionStatus(RawTestCaseResult result) {
        return switch (result.judge0StatusId() == null ? -1 : result.judge0StatusId()) {
            case 3 -> SubmissionStatus.ACCEPTED;
            case 4 -> SubmissionStatus.WRONG_ANSWER;
            case 5 -> SubmissionStatus.TIME_LIMIT_EXCEEDED;
            case 6 -> SubmissionStatus.COMPILE_ERROR;
            case 7, 8, 9, 10, 11, 12, 14 -> SubmissionStatus.RUNTIME_ERROR;
            case 13 -> SubmissionStatus.INTERNAL_ERROR;
            default -> SubmissionStatus.INTERNAL_ERROR;
        };
    }

    private boolean outputMatches(RawTestCaseResult result) {
        return normalizeOutput(result.stdout()).equals(normalizeOutput(result.expectedOutput()));
    }

    private String normalizeOutput(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n")
                .replace('\r', '\n')
                .stripTrailing();
    }

    private String verdictMessage(SubmissionStatus status, List<RawTestCaseResult> results) {
        if (status == SubmissionStatus.ACCEPTED) {
            return "Accepted";
        }
        if (status == SubmissionStatus.WRONG_ANSWER) {
            return "Wrong Answer";
        }

        return results.stream()
                .filter(result -> toSubmissionStatus(result) == status || !result.finished())
                .findFirst()
                .map(this::messageFor)
                .orElse(status.name());
    }

    private String messageFor(RawTestCaseResult result) {
        if (!isBlank(result.compileOutput())) {
            return result.compileOutput();
        }
        if (!isBlank(result.stderr())) {
            return result.stderr();
        }
        if (!isBlank(result.message())) {
            return result.message();
        }
        if (!result.finished()) {
            return "Judge0 execution did not finish before polling limit";
        }
        if (!isBlank(result.judge0StatusDescription())) {
            return result.judge0StatusDescription();
        }
        return "Execution finished";
    }

    private Integer runtimeMs(List<RawTestCaseResult> results) {
        return results.stream()
                .map(RawTestCaseResult::time)
                .filter(value -> !isBlank(value))
                .map(this::toMilliseconds)
                .filter(value -> value != null && value > 0)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private Integer memoryMb(List<RawTestCaseResult> results) {
        return results.stream()
                .map(RawTestCaseResult::memoryKb)
                .filter(value -> value != null && value > 0)
                .map(value -> (int) Math.ceil(value / 1024.0))
                .max(Comparator.naturalOrder())
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
