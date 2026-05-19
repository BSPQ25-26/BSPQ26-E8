package com.bspq26e8.backend.codeexecution;

import java.util.List;

public interface Judge0Client {

    List<Judge0SubmissionCreation> createBatch(List<Judge0SubmissionRequest> submissions);

    List<Judge0SubmissionResult> getBatch(List<String> tokens);

    record Judge0SubmissionRequest(
            Integer languageId,
            String sourceCode,
            String stdin,
            String expectedOutput
    ) {
    }

    record Judge0SubmissionCreation(
            String token,
            String errorMessage
    ) {

        public boolean created() {
            return token != null && !token.isBlank();
        }
    }

    record Judge0SubmissionResult(
            String token,
            Integer statusId,
            String statusDescription,
            String stdout,
            String stderr,
            String compileOutput,
            String message,
            String time,
            Integer memoryKb
    ) {

        public boolean finished() {
            return statusId != null && statusId > 2;
        }
    }
}
