package com.bspq26e8.backend.problem.service;

import com.bspq26e8.backend.problem.entity.Problem;
import com.bspq26e8.backend.problem.entity.ProblemDifficulty;
import com.bspq26e8.backend.problem.repository.ProblemLanguageRepository;
import com.bspq26e8.backend.problem.repository.ProblemRepository;
import com.bspq26e8.backend.user.entity.User;
import com.bspq26e8.backend.user.repository.UserRepository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProblemService {

	private final ProblemRepository problemRepository;
	private final ProblemLanguageRepository problemLanguageRepository;
	private final UserRepository userRepository;

	public ProblemService(
			ProblemRepository problemRepository,
			ProblemLanguageRepository problemLanguageRepository,
			UserRepository userRepository
	) {
		this.problemRepository = problemRepository;
		this.problemLanguageRepository = problemLanguageRepository;
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
		List<String> languages = resolveLanguagesByProblemIds(List.of(saved.getId()))
				.getOrDefault(saved.getId(), List.of());
		return CreateProblemResult.created(toSummary(saved, languages));
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

		List<Problem> problems = problemRepository.findPublicProblems(language, parsedDifficulty, authorId, author, name);
		return toSummaries(problems);
	}

	public List<ProblemSummary> listProblemsByAuthor(UUID authorId) {
		List<Problem> problems = problemRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
		return toSummaries(problems);
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
		List<String> languages = resolveLanguagesByProblemIds(List.of(saved.getId()))
				.getOrDefault(saved.getId(), List.of());
		return UpdateProblemResult.updated(toSummary(saved, languages));
	}

	public Optional<DeleteProblemError> deleteProblemByAuthor(UUID problemId, UUID authorId) {
		Optional<Problem> maybeProblem = problemRepository.findById(problemId);
		if (maybeProblem.isEmpty()) {
			return Optional.of(DeleteProblemError.NOT_FOUND);
		}

		Problem problem = maybeProblem.get();
		if (problem.getAuthor() == null || !problem.getAuthor().getId().equals(authorId)) {
			return Optional.of(DeleteProblemError.FORBIDDEN);
		}

		problemRepository.delete(problem);
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

	private List<ProblemSummary> toSummaries(List<Problem> problems) {
		Map<UUID, List<String>> languagesByProblemId = resolveLanguagesByProblemIds(
				problems.stream().map(Problem::getId).toList()
		);

		return problems.stream()
				.map(problem -> toSummary(problem, languagesByProblemId.getOrDefault(problem.getId(), List.of())))
				.toList();
	}

	private Map<UUID, List<String>> resolveLanguagesByProblemIds(Collection<UUID> problemIds) {
		if (problemIds == null || problemIds.isEmpty()) {
			return Map.of();
		}

		Map<UUID, LinkedHashSet<String>> grouped = new LinkedHashMap<>();
		problemLanguageRepository.findLanguageRowsByProblemIds(problemIds)
				.forEach(row -> grouped
						.computeIfAbsent(row.getProblemId(), key -> new LinkedHashSet<>())
						.add(row.getLanguageName()));

		Map<UUID, List<String>> result = new LinkedHashMap<>();
		grouped.forEach((problemId, languageNames) -> result.put(problemId, List.copyOf(languageNames)));
		return result;
	}

	private ProblemSummary toSummary(Problem problem, List<String> languages) {
		UUID authorId = problem.getAuthor() == null ? null : problem.getAuthor().getId();

		return new ProblemSummary(
				problem.getId(),
				problem.getSlug(),
				problem.getTitle(),
				problem.getDifficulty(),
				authorId,
				problem.getCreatedAt(),
				languages
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
			java.time.OffsetDateTime createdAt,
			List<String> languages
	) {
	}

	public enum DeleteProblemError {
		NOT_FOUND,
		FORBIDDEN
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
