package com.bspq26e8.backend.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Tag("integration")
public class IntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("app_db_test")
            .withUsername("test")q
            .withPassword("test");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;




    @Test
    void fullAuthAndProblemFlow() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String email = "integration_" + suffix + "@example.com";
        String username = "user_" + suffix;
        String password = "password123";

        LOGGER.info("Starting integration flow: user={}, problemSuffix={}", username, suffix);
        registerUser(email, username, password);
        String accessToken = loginUser(email, password);
        String problemId = createProblem(accessToken, suffix);
        assertProblemListed(problemId);
        createSubmission(accessToken, problemId, 1);
        deleteProblem(accessToken, problemId);
        LOGGER.info("Completed integration flow: user={}, problemId={}", username, problemId);
    }

    private void registerUser(String email, String username, String password) {
        LOGGER.info("Registering user: username={}, email={}", username, email);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/auth/register"),
                Map.of("email", email, "username", username, "password", password),
                Map.class
        );

        LOGGER.info("Register response: status={}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private String loginUser(String email, String password) {
        LOGGER.info("Logging in user: email={}", email);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/auth/login"),
                Map.of("email", email, "password", password),
                Map.class
        );

        LOGGER.info("Login response: status={}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        String accessToken = (String) response.getBody().get("accessToken");
        assertThat(accessToken).isNotBlank();
        LOGGER.info("Login token received: {} chars", accessToken.length());
        return accessToken;
    }

    private String createProblem(String accessToken, String suffix) {
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        Map<String, Object> problemRequest = Map.of(
                "slug", "test-problem-" + suffix,
                "title", "Integration Test Problem",
                "statementMd", "Solve the task.",
                "inputSpecMd", "Input details.",
                "outputSpecMd", "Output details.",
                "constraintsMd", "Constraints.",
                "hintsMd", "Hints.",
                "difficulty", "EASY",
                "solutionTemplate", "",
                "languageCompilationConfig", "{}"
        );

        LOGGER.info("Creating problem: slug={}", problemRequest.get("slug"));
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/problems"),
                HttpMethod.POST,
                new HttpEntity<>(problemRequest, authHeaders),
                Map.class
        );

        LOGGER.info("Create problem response: status={}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        String problemId = (String) response.getBody().get("id");
        assertThat(problemId).isNotBlank();
        LOGGER.info("Problem created: id={}", problemId);
        return problemId;
    }

    private void assertProblemListed(String problemId) {
        LOGGER.info("Listing problems to find id={}", problemId);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url("/api/problems"),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        LOGGER.info("List problems response: status={}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody())
                .anySatisfy(problem -> assertThat(problem.get("id")).isEqualTo(problemId));
    }

    private void deleteProblem(String accessToken, String problemId) {
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        LOGGER.info("Deleting problem: id={}", problemId);
        ResponseEntity<Void> response = restTemplate.exchange(
                url("/api/problems/" + problemId),
                HttpMethod.DELETE,
                new HttpEntity<>(authHeaders),
                Void.class
        );

        LOGGER.info("Delete problem response: status={}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


    private void createSubmission(String accessToken, String problemId, int languageId) {
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);

        LOGGER.info("Creating submission: problemId={}, languageId={}", problemId, languageId);
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/submissions"),
                HttpMethod.POST,
                new HttpEntity<>(Map.of(
                        "problemId", problemId,
                        "languageId", languageId,
                        "sourceCode", "print('Hello, World!')"
                ), authHeaders), Map.class
        );

        LOGGER.info("Create submission response: status={}", response.getStatusCode());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("QUEUED");
    }



    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
