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
import org.mockito.ArgumentCaptor;
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
    void updateSubmissionReturnsNotFoundWhenSubmissionDoesNotBelongToUser() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SubmissionService.UpdateSubmissionCommand command =
                new SubmissionService.UpdateSubmissionCommand(submissionId, userId, 2L, "new code");

        when(submissionRepository.findByIdAndUserId(submissionId, userId)).thenReturn(Optional.empty());

        SubmissionService.UpdateSubmissionResult result = submissionService.updateSubmission(command);

        assertFalse(result.updated());
        assertTrue(result.notFound());
        assertEquals("Submission not found for this user", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void updateSubmissionReturnsNotFoundWhenNewLanguageDoesNotExist() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Submission existingSubmission = org.mockito.Mockito.mock(Submission.class);

        SubmissionService.UpdateSubmissionCommand command =
                new SubmissionService.UpdateSubmissionCommand(submissionId, userId, 99L, "new code");

        when(submissionRepository.findByIdAndUserId(submissionId, userId)).thenReturn(Optional.of(existingSubmission));
        when(languageRepository.findById(99L)).thenReturn(Optional.empty());

        SubmissionService.UpdateSubmissionResult result = submissionService.updateSubmission(command);

        assertFalse(result.updated());
        assertTrue(result.notFound());
        assertEquals("Language not found", result.errorMessage());
        verify(submissionRepository, never()).save(any(Submission.class));
    }

    @Test
    void updateSubmissionUpdatesEditableFieldsAndSaves() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID problemId = UUID.randomUUID();
        Submission existingSubmission = org.mockito.Mockito.mock(Submission.class);
        Language newLanguage = org.mockito.Mockito.mock(Language.class);
        Submission savedSubmission = mockedSubmission(submissionId, userId, problemId, 2L, SubmissionStatus.QUEUED);

        SubmissionService.UpdateSubmissionCommand command =
                new SubmissionService.UpdateSubmissionCommand(submissionId, userId, 2L, "new code");

        when(submissionRepository.findByIdAndUserId(submissionId, userId)).thenReturn(Optional.of(existingSubmission));
        when(languageRepository.findById(2L)).thenReturn(Optional.of(newLanguage));
        when(submissionRepository.save(existingSubmission)).thenReturn(savedSubmission);

        SubmissionService.UpdateSubmissionResult result = submissionService.updateSubmission(command);

        assertTrue(result.updated());
        assertFalse(result.notFound());
        assertEquals(submissionId, result.submission().id());
        verify(existingSubmission).updateEditableFields(newLanguage, "new code");
        verify(submissionRepository).save(existingSubmission);
    }

    @Test
    void resubmitReturnsNotFoundWhenBaseSubmissionMissing() {
        UUID userId = UUID.randomUUID();
        UUID baseSubmissionId = UUID.randomUUID();

        SubmissionService.ResubmitCommand command =
                new SubmissionService.ResubmitCommand(baseSubmissionId, userId, null, null);

        when(submissionRepository.findByIdAndUserId(baseSubmissionId, userId)).thenReturn(Optional.empty());

        SubmissionService.CreateSubmissionResult result = submissionService.resubmit(command);

        assertFalse(result.created());
        assertTrue(result.notFound());
        assertEquals("Base submission not found for this user", result.errorMessage());
    }



    @Test
    void deleteSubmissionReturnsErrorWhenNotOwnedByUser() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(submissionRepository.findByIdAndUserId(submissionId, userId)).thenReturn(Optional.empty());

        Optional<String> result = submissionService.deleteSubmission(submissionId, userId);

        assertTrue(result.isPresent());
        assertEquals("Submission not found for this user", result.get());
    }

    @Test
    void deleteSubmissionDeletesWhenOwnedByUser() {
        UUID submissionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Submission submission = org.mockito.Mockito.mock(Submission.class);

        when(submissionRepository.findByIdAndUserId(submissionId, userId)).thenReturn(Optional.of(submission));

        Optional<String> result = submissionService.deleteSubmission(submissionId, userId);

        assertTrue(result.isEmpty());
        verify(submissionRepository).delete(submission);
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

