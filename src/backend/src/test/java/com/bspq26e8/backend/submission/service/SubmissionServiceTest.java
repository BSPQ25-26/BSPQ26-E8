package com.bspq26e8.backend.submission.service;

import com.bspq26e8.backend.problem.entity.Language;
import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.problem.repository.LanguageRepository;
import com.bspq26e8.backend.problem.repository.ProblemRepository;
import com.bspq26e8.backend.submission.entity.Submission;
import com.bspq26e8.backend.submission.entity.SubmissionStatus;
import com.bspq26e8.backend.submission.repository.SubmissionRepository;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private SubmissionService submissionService;

    @Test
    void createSubmissionReturnsNotFoundWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        SubmissionService.CreateSubmissionCommand command =
                new SubmissionService.CreateSubmissionCommand(userId, UUID.randomUUID(), 1L, "code");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        SubmissionService.CreateSubmissionResult result = submissionService.createSubmission(command);

        assertFalse(result.created());
        assertTrue(result.notFound());
        assertEquals("User not found", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void createSubmissionReturnsNotFoundWhenProblemDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);

        SubmissionService.CreateSubmissionCommand command =
                new SubmissionService.CreateSubmissionCommand(userId, problemId, 1L, "code");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        SubmissionService.CreateSubmissionResult result = submissionService.createSubmission(command);

        assertFalse(result.created());
        assertTrue(result.notFound());
        assertEquals("Problem not found", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void createSubmissionReturnsNotFoundWhenLanguageDoesNotExist() {
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        Problem problem = org.mockito.Mockito.mock(Problem.class);

        SubmissionService.CreateSubmissionCommand command =
                new SubmissionService.CreateSubmissionCommand(userId, problemId, 9L, "code");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(languageRepository.findById(9L)).thenReturn(Optional.empty());

        SubmissionService.CreateSubmissionResult result = submissionService.createSubmission(command);

        assertFalse(result.created());
        assertTrue(result.notFound());
        assertEquals("Language not found", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void createSubmissionCreatesSubmissionWhenDependenciesExist() {
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        User user = org.mockito.Mockito.mock(User.class);
        Problem problem = org.mockito.Mockito.mock(Problem.class);
        Language language = org.mockito.Mockito.mock(Language.class);
        Submission savedSubmission = mockedSubmission(UUID.randomUUID(), userId, problemId, 1L, SubmissionStatus.QUEUED);

        SubmissionService.CreateSubmissionCommand command =
                new SubmissionService.CreateSubmissionCommand(userId, problemId, 1L, "print('hello')");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(problem));
        when(languageRepository.findById(1L)).thenReturn(Optional.of(language));
        when(submissionRepository.save(any(Submission.class))).thenReturn(savedSubmission);

        SubmissionService.CreateSubmissionResult result = submissionService.createSubmission(command);

        assertTrue(result.created());
        assertFalse(result.notFound());
        assertNotNull(result.submission());
        assertEquals(savedSubmission.getId(), result.submission().id());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void listMineParsesStatusAndMapsSubmissions() {
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        Submission submission = mockedSubmission(UUID.randomUUID(), userId, problemId, 1L, SubmissionStatus.ACCEPTED);

        when(submissionRepository.findMine(userId, problemId, "accepted")).thenReturn(List.of(submission));

        List<SubmissionService.SubmissionView> result = submissionService.listMine(userId, problemId, "ACCEPTED");

        assertEquals(1, result.size());
        assertEquals(SubmissionStatus.ACCEPTED, result.getFirst().status());
        verify(submissionRepository).findMine(userId, problemId, "accepted");
    }

    @Test
    void listByProblemIgnoresInvalidStatus() {
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        Submission submission = mockedSubmission(UUID.randomUUID(), userId, problemId, 1L, SubmissionStatus.QUEUED);

        when(submissionRepository.findByProblem(problemId, null)).thenReturn(List.of(submission));

        List<SubmissionService.SubmissionView> result = submissionService.listByProblem(problemId, "not-valid");

        assertEquals(1, result.size());
        verify(submissionRepository).findByProblem(problemId, null);
    }

    @Test
    void markExecutionStartedReturnsNotFoundWhenSubmissionDoesNotExist() {
        UUID submissionId = UUID.randomUUID();

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        SubmissionService.StartExecutionResult result = submissionService.markExecutionStarted(submissionId);

        assertFalse(result.started());
        assertTrue(result.notFound());
        assertEquals("Submission not found", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void markExecutionStartedMarksQueuedSubmissionRunningAndSaves() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        Submission existingSubmission = org.mockito.Mockito.mock(Submission.class);
        Submission savedSubmission = mockedSubmission(submissionId, userId, problemId, 1L, SubmissionStatus.RUNNING);

        when(existingSubmission.getStatus()).thenReturn(SubmissionStatus.QUEUED);
        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(existingSubmission));
        when(submissionRepository.save(existingSubmission)).thenReturn(savedSubmission);

        SubmissionService.StartExecutionResult result = submissionService.markExecutionStarted(submissionId);

        assertTrue(result.started());
        assertFalse(result.notFound());
        assertEquals(SubmissionStatus.RUNNING, result.submission().status());
        verify(existingSubmission).markRunning();
        verify(submissionRepository).save(existingSubmission);
    }

    @Test
    void applyExecutionResultReturnsNotFoundWhenSubmissionDoesNotExist() {
        UUID submissionId = UUID.randomUUID();
        SubmissionService.ApplyExecutionResultCommand command =
                new SubmissionService.ApplyExecutionResultCommand(
                        submissionId,
                        SubmissionStatus.ACCEPTED,
                        "Accepted",
                        42,
                        128,
                        10,
                        10
                );

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.empty());

        SubmissionService.ApplyExecutionResultResult result = submissionService.applyExecutionResult(command);

        assertFalse(result.updated());
        assertTrue(result.notFound());
        assertEquals("Submission not found", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void applyExecutionResultUpdatesResultFieldsAndSaves() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        Submission existingSubmission = org.mockito.Mockito.mock(Submission.class);
        Submission savedSubmission = mockedSubmission(submissionId, userId, problemId, 1L, SubmissionStatus.ACCEPTED);

        SubmissionService.ApplyExecutionResultCommand command =
                new SubmissionService.ApplyExecutionResultCommand(
                        submissionId,
                        SubmissionStatus.ACCEPTED,
                        "Accepted",
                        42,
                        128,
                        10,
                        10
                );

        when(submissionRepository.findById(submissionId)).thenReturn(Optional.of(existingSubmission));
        when(submissionRepository.save(existingSubmission)).thenReturn(savedSubmission);

        SubmissionService.ApplyExecutionResultResult result = submissionService.applyExecutionResult(command);

        assertTrue(result.updated());
        assertFalse(result.notFound());
        assertEquals(submissionId, result.submission().id());
        assertEquals(SubmissionStatus.ACCEPTED, result.submission().status());
        verify(existingSubmission).applyExecutionResult(
                SubmissionStatus.ACCEPTED,
                "Accepted",
                42,
                128,
                10,
                10
        );
        verify(submissionRepository).save(existingSubmission);
    }

    @Test
    void parseStatusHandlesValidAndInvalidValues() {
        assertTrue(submissionService.parseStatus("accepted").isPresent());
        assertEquals(SubmissionStatus.ACCEPTED, submissionService.parseStatus("accepted").get());
        assertTrue(submissionService.parseStatus(" ").isEmpty());
        assertTrue(submissionService.parseStatus("does-not-exist").isEmpty());
    }

    private Submission mockedSubmission(
            UUID submissionId,
            UUID userId,
            UUID problemId,
            Long languageId,
            SubmissionStatus status
    ) {
        User user = org.mockito.Mockito.mock(User.class);
        Problem problem = org.mockito.Mockito.mock(Problem.class);
        Language language = org.mockito.Mockito.mock(Language.class);
        Submission submission = org.mockito.Mockito.mock(Submission.class);

        when(user.getId()).thenReturn(userId);
        when(problem.getId()).thenReturn(problemId);
        when(language.getId()).thenReturn(languageId);

        when(submission.getId()).thenReturn(submissionId);
        when(submission.getUser()).thenReturn(user);
        when(submission.getProblem()).thenReturn(problem);
        when(submission.getLanguage()).thenReturn(language);
        when(submission.getStatus()).thenReturn(status);
        when(submission.getVerdictMessage()).thenReturn(null);
        when(submission.getRuntimeMs()).thenReturn(null);
        when(submission.getMemoryMb()).thenReturn(null);
        when(submission.getTestcasesPassed()).thenReturn(0);
        when(submission.getTestcasesTotal()).thenReturn(null);
        when(submission.getSubmittedAt()).thenReturn(OffsetDateTime.now());
        when(submission.getEvaluatedAt()).thenReturn(null);

        return submission;
    }
}

