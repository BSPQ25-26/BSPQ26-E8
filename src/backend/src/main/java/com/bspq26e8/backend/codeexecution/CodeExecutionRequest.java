package com.bspq26e8.backend.codeexecution;

import java.util.List;
import java.util.UUID;

public record CodeExecutionRequest(
        UUID submissionId,
        String sourceCode,
        LanguageSpec language,
        ProblemSpec problem,
        List<TestCaseSpec> testCases,
        CodeExecutionOptions options
) {

    public record LanguageSpec(
            Long id,
            String code,
            String name,
            String compileCommand,
            String runCommand
    ) {
    }

    public record ProblemSpec(
            UUID id,
            String solutionTemplate,
            String languageCompilationConfig
    ) {
    }

    public record TestCaseSpec(
            String inputData,
            String expectedOutput,
            boolean sample
    ) {
    }
}
