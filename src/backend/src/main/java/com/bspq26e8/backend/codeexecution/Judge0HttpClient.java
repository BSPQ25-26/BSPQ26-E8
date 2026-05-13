package com.bspq26e8.backend.codeexecution;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class Judge0HttpClient implements Judge0Client {

    private static final String RESULT_FIELDS = "token,stdout,stderr,compile_output,message,time,memory,status";

    private final RestClient restClient;
    private final Judge0Properties properties;

    public Judge0HttpClient(RestClient.Builder restClientBuilder, Judge0Properties properties) {
        this.properties = properties;

        RestClient.Builder builder = restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (!isBlank(properties.getApiKey())) {
            builder.defaultHeader("X-RapidAPI-Key", properties.getApiKey());
        }
        if (!isBlank(properties.getApiHost())) {
            builder.defaultHeader("X-RapidAPI-Host", properties.getApiHost());
        }
        if (!isBlank(properties.getAuthorizationToken())) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, properties.getAuthorizationToken());
        }

        this.restClient = builder.build();
    }

    @Override
    public List<Judge0SubmissionCreation> createBatch(List<Judge0SubmissionRequest> submissions) {
        Judge0BatchCreateResponseItem[] response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions/batch")
                        .queryParam("base64_encoded", properties.isBase64Encoded())
                        .build())
                .body(new Judge0BatchCreateRequest(toHttpRequests(submissions)))
                .retrieve()
                .body(Judge0BatchCreateResponseItem[].class);

        if (response == null) {
            return List.of();
        }

        return List.of(response).stream()
                .map(item -> new Judge0SubmissionCreation(item.token(), item.errorMessage()))
                .toList();
    }

    @Override
    public List<Judge0SubmissionResult> getBatch(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return List.of();
        }

        Judge0BatchResultResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/submissions/batch")
                        .queryParam("tokens", String.join(",", tokens))
                        .queryParam("base64_encoded", properties.isBase64Encoded())
                        .queryParam("fields", RESULT_FIELDS)
                        .build())
                .retrieve()
                .body(Judge0BatchResultResponse.class);

        if (response == null || response.submissions() == null) {
            return List.of();
        }

        return response.submissions().stream()
                .filter(Objects::nonNull)
                .map(this::toDomainResult)
                .toList();
    }

    private List<Judge0HttpSubmissionRequest> toHttpRequests(List<Judge0SubmissionRequest> submissions) {
        return submissions.stream()
                .map(submission -> new Judge0HttpSubmissionRequest(
                        submission.languageId(),
                        encodeText(submission.sourceCode()),
                        encodeText(submission.stdin()),
                        encodeText(submission.expectedOutput())
                ))
                .toList();
    }

    private Judge0SubmissionResult toDomainResult(Judge0SubmissionResultResponse response) {
        Judge0StatusResponse status = response.status();
        return new Judge0SubmissionResult(
                response.token(),
                status == null ? null : status.id(),
                status == null ? null : status.description(),
                decodeText(response.stdout()),
                decodeText(response.stderr()),
                decodeText(response.compileOutput()),
                decodeText(response.message()),
                response.time(),
                response.memory()
        );
    }

    private String encodeText(String value) {
        if (!properties.isBase64Encoded() || value == null) {
            return value;
        }
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeText(String value) {
        if (!properties.isBase64Encoded() || value == null) {
            return value;
        }
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record Judge0BatchCreateRequest(
            List<Judge0HttpSubmissionRequest> submissions
    ) {
    }

    private record Judge0HttpSubmissionRequest(
            @JsonProperty("language_id") Integer languageId,
            @JsonProperty("source_code") String sourceCode,
            String stdin,
            @JsonProperty("expected_output") String expectedOutput
    ) {
    }

    private record Judge0BatchCreateResponseItem(
            String token,
            @JsonProperty("error") String directError,
            @JsonProperty("language_id") List<String> languageIdErrors,
            @JsonProperty("source_code") List<String> sourceCodeErrors
    ) {

        String errorMessage() {
            if (!isEmpty(languageIdErrors)) {
                return String.join(", ", languageIdErrors);
            }
            if (!isEmpty(sourceCodeErrors)) {
                return String.join(", ", sourceCodeErrors);
            }
            return directError;
        }

        private boolean isEmpty(List<String> errors) {
            return errors == null || errors.isEmpty();
        }
    }

    private record Judge0BatchResultResponse(
            List<Judge0SubmissionResultResponse> submissions
    ) {
    }

    private record Judge0SubmissionResultResponse(
            String token,
            String stdout,
            String stderr,
            @JsonProperty("compile_output") String compileOutput,
            String message,
            String time,
            Integer memory,
            Judge0StatusResponse status
    ) {
    }

    private record Judge0StatusResponse(
            Integer id,
            String description
    ) {
    }
}
