package com.bspq26e8.backend.problem.service;

import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.problem.entity.ProblemDifficulty;
import com.bspq26e8.backend.problem.repository.ProblemRepository;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import com.bspq26e8.backend.problem.repository.ProblemLanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProblemServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemLanguageRepository problemLanguageRepository;

    @InjectMocks
    private ProblemService problemService;

    @Test
    void createProblem_Success() {
        ProblemService.CreateProblemCommand command = new ProblemService.CreateProblemCommand(
                "test-slug", "Test Title", "statement", "input", "output", "constraints", "hints",
                ProblemDifficulty.EASY, UUID.randomUUID(), "template", "config"
        );
        User mockUser = mock(User.class);
        when(userRepository.findById(command.authorId())).thenReturn(Optional.of(mockUser));
        when(problemRepository.existsBySlug(command.slug())).thenReturn(false);

        Problem savedProblem = mock(Problem.class);
        when(savedProblem.getId()).thenReturn(UUID.randomUUID());
        when(savedProblem.getSlug()).thenReturn(command.slug());
        when(savedProblem.getTitle()).thenReturn(command.title());
        when(savedProblem.getDifficulty()).thenReturn(command.difficulty());
        when(savedProblem.getAuthor()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(command.authorId());
        when(savedProblem.getCreatedAt()).thenReturn(OffsetDateTime.now());

        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);

        ProblemService.CreateProblemResult result = problemService.createProblem(command);

        assertTrue(result.created());
        assertFalse(result.notFound());
        assertNull(result.errorMessage());
        assertNotNull(result.problem());
        assertEquals(command.slug(), result.problem().slug());

        verify(problemRepository).save(any(Problem.class));
    }


    @Test
    void createProblem_AuthorNotFound() {
        ProblemService.CreateProblemCommand command = new ProblemService.CreateProblemCommand(
                "test-slug", "Test Title", "statement", "input", "output", "constraints", "hints",
                ProblemDifficulty.EASY, UUID.randomUUID(), "template", "config"
        );
        when(problemRepository.existsBySlug(command.slug())).thenReturn(false);
        when(userRepository.findById(command.authorId())).thenReturn(Optional.empty());

        ProblemService.CreateProblemResult result = problemService.createProblem(command);

        assertFalse(result.created());
        assertTrue(result.notFound());
        assertEquals("Author not found", result.errorMessage());

        verify(problemRepository, never()).save(any(Problem.class));
    }

    @Test
    void createProblem_NullAuthorId() {
        ProblemService.CreateProblemCommand command = new ProblemService.CreateProblemCommand(
                "test-slug", "Test Title", "statement", "input", "output", "constraints", "hints",
                ProblemDifficulty.EASY, null, "template", "config"
        );
        when(problemRepository.existsBySlug(command.slug())).thenReturn(false);

        Problem savedProblem = mock(Problem.class);
        when(savedProblem.getId()).thenReturn(UUID.randomUUID());
        when(savedProblem.getSlug()).thenReturn(command.slug());
        when(savedProblem.getTitle()).thenReturn(command.title());
        when(savedProblem.getDifficulty()).thenReturn(command.difficulty());
        when(savedProblem.getAuthor()).thenReturn(null);
        when(savedProblem.getCreatedAt()).thenReturn(OffsetDateTime.now());

        when(problemRepository.save(any(Problem.class))).thenReturn(savedProblem);

        ProblemService.CreateProblemResult result = problemService.createProblem(command);

        assertTrue(result.created());
        assertFalse(result.notFound());
        assertNull(result.errorMessage());
        assertNotNull(result.problem());
        assertNull(result.problem().authorId());

        verify(problemRepository).save(any(Problem.class));
    }

    @Test
    void listPublicProblems() {
        String language = "java";
        String difficultyStr = "easy";
        UUID authorId = UUID.randomUUID();
        String authorName = "John";
        String name = "Test";

        Problem mockProblem = mock(Problem.class);
        User mockUser = mock(User.class);
        when(mockProblem.getAuthor()).thenReturn(mockUser);
        when(mockUser.getId()).thenReturn(authorId);
        when(mockProblem.getId()).thenReturn(UUID.randomUUID());

        when(problemRepository.findPublicProblems(eq(language), eq("easy"), eq(authorId), eq(authorName), eq(name)))
                .thenReturn(List.of(mockProblem));

        List<ProblemService.ProblemSummary> result = problemService.listPublicProblems(language, difficultyStr, authorId, authorName, name);

        assertEquals(1, result.size());
    }

    @Test
    void listProblemsByAuthor() {
        UUID authorId = UUID.randomUUID();
        Problem mockProblem = mock(Problem.class);
        when(mockProblem.getId()).thenReturn(UUID.randomUUID());

        when(problemRepository.findByAuthorIdOrderByCreatedAtDesc(authorId))
                .thenReturn(List.of(mockProblem));

        List<ProblemService.ProblemSummary> result = problemService.listProblemsByAuthor(authorId);

        assertEquals(1, result.size());
    }

    @Test
    void updateProblemByAuthor_Success() {
        UUID problemId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        ProblemService.UpdateProblemCommand command = new ProblemService.UpdateProblemCommand(
                authorId, "new-slug", "New Title", "statement", "input", "output", "constraints", "hints",
                ProblemDifficulty.MEDIUM, "template", "config"
        );

        Problem mockProblem = mock(Problem.class);
        User mockAuthor = mock(User.class);

        when(mockProblem.getAuthor()).thenReturn(mockAuthor);
        when(mockAuthor.getId()).thenReturn(authorId);
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(mockProblem));
        when(problemRepository.existsBySlugAndIdNot(command.slug(), problemId)).thenReturn(false);

        // Mock toSummary
        when(mockProblem.getId()).thenReturn(problemId);
        when(mockProblem.getSlug()).thenReturn(command.slug());
        when(mockProblem.getTitle()).thenReturn(command.title());
        when(mockProblem.getDifficulty()).thenReturn(command.difficulty());
        when(mockProblem.getCreatedAt()).thenReturn(OffsetDateTime.now());

        when(problemRepository.save(mockProblem)).thenReturn(mockProblem);

        ProblemService.UpdateProblemResult result = problemService.updateProblemByAuthor(problemId, command);

        assertTrue(result.updated());
        assertFalse(result.notFound());
        assertFalse(result.forbidden());
        assertEquals(command.slug(), result.problem().slug());

        verify(mockProblem).updateEditableFields(
                command.slug(), command.title(), command.statementMd(), command.inputSpecMd(),
                command.outputSpecMd(), command.constraintsMd(), command.hintsMd(), command.difficulty(),
                command.solutionTemplate(), command.languageCompilationConfig()
        );
        verify(problemRepository).save(mockProblem);
    }

    @Test
    void updateProblemByAuthor_NotFound() {
        UUID problemId = UUID.randomUUID();
        ProblemService.UpdateProblemCommand command = new ProblemService.UpdateProblemCommand(
                UUID.randomUUID(), "new-slug", "New Title", "statement", "input", "output", "constraints", "hints",
                ProblemDifficulty.MEDIUM, "template", "config"
        );

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        ProblemService.UpdateProblemResult result = problemService.updateProblemByAuthor(problemId, command);

        assertFalse(result.updated());
        assertTrue(result.notFound());
        assertFalse(result.forbidden());
        assertEquals("Problem not found", result.errorMessage());
    }

    @Test
    void deleteProblemByAuthor_Success() {
        UUID problemId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Problem mockProblem = mock(Problem.class);
        User mockAuthor = mock(User.class);
        when(mockProblem.getAuthor()).thenReturn(mockAuthor);
        when(mockAuthor.getId()).thenReturn(authorId);
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(mockProblem));

        Optional<ProblemService.DeleteProblemError> result = problemService.deleteProblemByAuthor(problemId, authorId);

        assertTrue(result.isEmpty());
        verify(problemRepository).delete(mockProblem);
    }

    @Test
    void deleteProblemByAuthor_NotFound() {
        UUID problemId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        when(problemRepository.findById(problemId)).thenReturn(Optional.empty());

        Optional<ProblemService.DeleteProblemError> result = problemService.deleteProblemByAuthor(problemId, authorId);

        assertTrue(result.isPresent());
        assertEquals(ProblemService.DeleteProblemError.NOT_FOUND, result.get());
        verify(problemRepository, never()).delete(any());
    }

    @Test
    void deleteProblemByAuthor_Forbidden() {
        UUID problemId = UUID.randomUUID();
        UUID actualAuthorId = UUID.randomUUID();
        UUID anotherAuthorId = UUID.randomUUID();

        Problem mockProblem = mock(Problem.class);
        User mockAuthor = mock(User.class);
        when(mockProblem.getAuthor()).thenReturn(mockAuthor);
        when(mockAuthor.getId()).thenReturn(actualAuthorId);
        when(problemRepository.findById(problemId)).thenReturn(Optional.of(mockProblem));

        Optional<ProblemService.DeleteProblemError> result = problemService.deleteProblemByAuthor(problemId, anotherAuthorId);

        assertTrue(result.isPresent());
        assertEquals(ProblemService.DeleteProblemError.FORBIDDEN, result.get());
        verify(problemRepository, never()).delete(any());
    }

    @Test
    void parseDifficulty_Success() {
        Optional<ProblemDifficulty> result = problemService.parseDifficulty("easy");
        assertTrue(result.isPresent());
        assertEquals(ProblemDifficulty.EASY, result.get());

        result = problemService.parseDifficulty("  MEDIUM  ");
        assertTrue(result.isPresent());
        assertEquals(ProblemDifficulty.MEDIUM, result.get());

        result = problemService.parseDifficulty("HARD");
        assertTrue(result.isPresent());
        assertEquals(ProblemDifficulty.HARD, result.get());
    }

    @Test
    void parseDifficulty_NullOrBlank() {
        assertTrue(problemService.parseDifficulty(null).isEmpty());
        assertTrue(problemService.parseDifficulty("").isEmpty());
        assertTrue(problemService.parseDifficulty("   ").isEmpty());
    }

    @Test
    void parseDifficulty_Invalid() {
        assertTrue(problemService.parseDifficulty("IMPOSSIBLE").isEmpty());
    }
}
