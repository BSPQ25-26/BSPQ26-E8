package com.bspq26e8.backend.problem.service;

import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.problem.entity.ProblemDifficulty;
import com.bspq26e8.backend.problem.repository.ProblemRepository;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {

	private final ProblemRepository problemRepository;
	private final UserRepository userRepository;

	public ProblemService(ProblemRepository problemRepository, UserRepository userRepository) {
		this.problemRepository = problemRepository;
		this.userRepository = userRepository;
	}

	public CreateProblemResult createProblem(CreateProblemCommand command) {
		if (problemRepository.existsBySlug(command.slug())) {
			return CreateProblemResult.conflict("A problem with that slug already exists");
		}

		Optional<User> author = resolveAuthor(command.authorId());

		if (command.authorId() != null && author.isEmpty()) {
			return CreateProblemResult.notFound("Author not found");
		}

		Problem problem = new Problem(
				command.slug(),
				command.title(),
				command.statementMd(),
				command.inputSpecMd(),
				command.outputSpecMd(),
				command.constraintsMd(),
				command.hintsMd(),
				command.difficulty(),
				author.orElse(null),
				command.solutionTemplate(),
				command.languageCompilationConfig()
		);

		Problem saved = problemRepository.save(problem);
		return CreateProblemResult.created(toSummary(saved));
	}

	public List<ProblemSummary> listPublicProblems(
			String language,
			String difficulty,
			UUID authorId,
			String author,
			String name
	) {
		String parsedDifficulty = parseDifficulty(difficulty)
				.map(value -> value.name().toLowerCase())
				.orElse(null);

		return problemRepository.findPublicProblems(language, parsedDifficulty, authorId, author, name)
				.stream()
				.map(this::toSummary)
				.toList();
	}

	public List<ProblemSummary> listProblemsByAuthor(UUID authorId) {
		return problemRepository.findByAuthorIdOrderByCreatedAtDesc(authorId)
				.stream()
				.map(this::toSummary)
				.toList();
	}

	public UpdateProblemResult updateProblemByAuthor(UUID problemId, UpdateProblemCommand command) {
		Optional<Problem> maybeProblem = problemRepository.findById(problemId);

		if (maybeProblem.isEmpty()) {
			return UpdateProblemResult.notFound("Problem not found");
		}

		Problem problem = maybeProblem.get();
		if (problem.getAuthor() == null || !problem.getAuthor().getId().equals(command.authorId())) {
			return UpdateProblemResult.forbidden("Only the author can edit this problem");
		}

		if (command.slug() != null && problemRepository.existsBySlugAndIdNot(command.slug(), problemId)) {
			return UpdateProblemResult.conflict("A problem with that slug already exists");
		}

		problem.updateEditableFields(
				command.slug(),
				command.title(),
				command.statementMd(),
				command.inputSpecMd(),
				command.outputSpecMd(),
				command.constraintsMd(),
				command.hintsMd(),
				command.difficulty(),
				command.solutionTemplate(),
				command.languageCompilationConfig()
		);

		Problem saved = problemRepository.save(problem);
		return UpdateProblemResult.updated(toSummary(saved));
	}

	public Optional<DeleteProblemError> deleteProblem(UUID problemId) {
		if (!problemRepository.existsById(problemId)) {
			return Optional.of(DeleteProblemError.NOT_FOUND);
		}

		problemRepository.deleteById(problemId);
		return Optional.empty();
	}

	public Optional<ProblemDifficulty> parseDifficulty(String rawDifficulty) {
		if (rawDifficulty == null || rawDifficulty.isBlank()) {
			return Optional.empty();
		}

		try {
			return Optional.of(ProblemDifficulty.valueOf(rawDifficulty.trim().toUpperCase()));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private Optional<User> resolveAuthor(UUID authorId) {
		if (authorId == null) {
			return Optional.empty();
		}

		return userRepository.findById(authorId);
	}

	private ProblemSummary toSummary(Problem problem) {
		UUID authorId = problem.getAuthor() == null ? null : problem.getAuthor().getId();

		return new ProblemSummary(
				problem.getId(),
				problem.getSlug(),
				problem.getTitle(),
				problem.getDifficulty(),
				authorId,
				problem.getCreatedAt()
		);
	}

	public record CreateProblemCommand(
			String slug,
			String title,
			String statementMd,
			String inputSpecMd,
			String outputSpecMd,
			String constraintsMd,
			String hintsMd,
			ProblemDifficulty difficulty,
			UUID authorId,
			String solutionTemplate,
			String languageCompilationConfig
	) {
	}

	public record ProblemSummary(
			UUID id,
			String slug,
			String title,
			ProblemDifficulty difficulty,
			UUID authorId,
			java.time.OffsetDateTime createdAt
	) {
	}

	public enum DeleteProblemError {
		NOT_FOUND
	}

	public record UpdateProblemCommand(
			UUID authorId,
			String slug,
			String title,
			String statementMd,
			String inputSpecMd,
			String outputSpecMd,
			String constraintsMd,
			String hintsMd,
			ProblemDifficulty difficulty,
			String solutionTemplate,
			String languageCompilationConfig
	) {
	}

	public record UpdateProblemResult(
			boolean updated,
			boolean notFound,
			boolean forbidden,
			String errorMessage,
			ProblemSummary problem
	) {

		public static UpdateProblemResult updated(ProblemSummary problem) {
			return new UpdateProblemResult(true, false, false, null, problem);
		}

		public static UpdateProblemResult notFound(String message) {
			return new UpdateProblemResult(false, true, false, message, null);
		}

		public static UpdateProblemResult forbidden(String message) {
			return new UpdateProblemResult(false, false, true, message, null);
		}

		public static UpdateProblemResult conflict(String message) {
			return new UpdateProblemResult(false, false, false, message, null);
		}
	}

	public record CreateProblemResult(boolean created, boolean notFound, String errorMessage, ProblemSummary problem) {

		public static CreateProblemResult created(ProblemSummary problem) {
			return new CreateProblemResult(true, false, null, problem);
		}

		public static CreateProblemResult conflict(String message) {
			return new CreateProblemResult(false, false, message, null);
		}

		public static CreateProblemResult notFound(String message) {
			return new CreateProblemResult(false, true, message, null);
		}
	}
}
