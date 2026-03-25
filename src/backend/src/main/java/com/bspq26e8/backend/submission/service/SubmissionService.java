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
import org.springframework.stereotype.Service;

@Service
public class SubmissionService {

	private final SubmissionRepository submissionRepository;
	private final UserRepository userRepository;
	private final ProblemRepository problemRepository;
	private final LanguageRepository languageRepository;

	public SubmissionService(
			SubmissionRepository submissionRepository,
			UserRepository userRepository,
			ProblemRepository problemRepository,
			LanguageRepository languageRepository
	) {
		this.submissionRepository = submissionRepository;
		this.userRepository = userRepository;
		this.problemRepository = problemRepository;
		this.languageRepository = languageRepository;
	}

	public CreateSubmissionResult createSubmission(CreateSubmissionCommand command) {
		Optional<User> user = userRepository.findById(command.userId());
		if (user.isEmpty()) {
			return CreateSubmissionResult.notFound("User not found");
		}

		Optional<Problem> problem = problemRepository.findById(command.problemId());
		if (problem.isEmpty()) {
			return CreateSubmissionResult.notFound("Problem not found");
		}

		Optional<Language> language = languageRepository.findById(command.languageId());
		if (language.isEmpty()) {
			return CreateSubmissionResult.notFound("Language not found");
		}

		Submission submission = new Submission(user.get(), problem.get(), language.get(), command.sourceCode());
		Submission saved = submissionRepository.save(submission);

		return CreateSubmissionResult.created(toView(saved));
	}

	public List<SubmissionView> listMine(UUID userId, UUID problemId, String status) {
		String dbStatus = parseStatus(status).map(value -> value.name().toLowerCase()).orElse(null);

		return submissionRepository.findMine(userId, problemId, dbStatus)
				.stream()
				.map(this::toView)
				.toList();
	}

	public List<SubmissionView> listByProblem(UUID problemId, String status) {
		String dbStatus = parseStatus(status).map(value -> value.name().toLowerCase()).orElse(null);

		return submissionRepository.findByProblem(problemId, dbStatus)
				.stream()
				.map(this::toView)
				.toList();
	}

	public Optional<SubmissionView> findLatestMine(UUID userId, UUID problemId) {
		return submissionRepository.findLatestMine(userId, problemId).map(this::toView);
	}

	public Optional<SubmissionView> findBestMineByProblem(UUID userId, UUID problemId) {
		return submissionRepository.findBestMineByProblem(userId, problemId).map(this::toView);
	}

	public UpdateSubmissionResult updateSubmission(UpdateSubmissionCommand command) {
		Optional<Submission> mine = submissionRepository.findByIdAndUserId(command.submissionId(), command.userId());
		if (mine.isEmpty()) {
			return UpdateSubmissionResult.notFound("Submission not found for this user");
		}

		Submission submission = mine.get();

		Language newLanguage = null;
		if (command.languageId() != null) {
			Optional<Language> language = languageRepository.findById(command.languageId());
			if (language.isEmpty()) {
				return UpdateSubmissionResult.notFound("Language not found");
			}
			newLanguage = language.get();
		}

		submission.updateEditableFields(newLanguage, command.sourceCode());
		Submission saved = submissionRepository.save(submission);

		return UpdateSubmissionResult.updated(toView(saved));
	}

	public CreateSubmissionResult resubmit(ResubmitCommand command) {
		Optional<Submission> mine = submissionRepository.findByIdAndUserId(command.baseSubmissionId(), command.userId());
		if (mine.isEmpty()) {
			return CreateSubmissionResult.notFound("Base submission not found for this user");
		}

		Submission base = mine.get();

		Language language = base.getLanguage();
		if (command.languageId() != null) {
			Optional<Language> maybeLanguage = languageRepository.findById(command.languageId());
			if (maybeLanguage.isEmpty()) {
				return CreateSubmissionResult.notFound("Language not found");
			}
			language = maybeLanguage.get();
		}

		String sourceCode = command.sourceCode() == null ? base.getSourceCode() : command.sourceCode();
		Submission newSubmission = new Submission(base.getUser(), base.getProblem(), language, sourceCode);
		Submission saved = submissionRepository.save(newSubmission);

		return CreateSubmissionResult.created(toView(saved));
	}

	public Optional<String> deleteSubmission(UUID submissionId, UUID userId) {
		Optional<Submission> mine = submissionRepository.findByIdAndUserId(submissionId, userId);
		if (mine.isEmpty()) {
			return Optional.of("Submission not found for this user");
		}

		submissionRepository.delete(mine.get());
		return Optional.empty();
	}

	public Optional<SubmissionStatus> parseStatus(String rawStatus) {
		if (rawStatus == null || rawStatus.isBlank()) {
			return Optional.empty();
		}

		try {
			return Optional.of(SubmissionStatus.valueOf(rawStatus.trim().toUpperCase()));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private SubmissionView toView(Submission submission) {
		return new SubmissionView(
				submission.getId(),
				submission.getUser().getId(),
				submission.getProblem().getId(),
				submission.getLanguage().getId(),
				submission.getStatus(),
				submission.getVerdictMessage(),
				submission.getRuntimeMs(),
				submission.getMemoryMb(),
				submission.getTestcasesPassed(),
				submission.getTestcasesTotal(),
				submission.getSubmittedAt(),
				submission.getEvaluatedAt()
		);
	}

	public record SubmissionView(
			UUID id,
			UUID userId,
			UUID problemId,
			Long languageId,
			SubmissionStatus status,
			String verdictMessage,
			Integer runtimeMs,
			Integer memoryMb,
			int testcasesPassed,
			Integer testcasesTotal,
			OffsetDateTime submittedAt,
			OffsetDateTime evaluatedAt
	) {
	}

	public record CreateSubmissionCommand(UUID userId, UUID problemId, Long languageId, String sourceCode) {
	}

	public record UpdateSubmissionCommand(UUID submissionId, UUID userId, Long languageId, String sourceCode) {
	}

	public record ResubmitCommand(UUID baseSubmissionId, UUID userId, Long languageId, String sourceCode) {
	}

	public record CreateSubmissionResult(boolean created, boolean notFound, String errorMessage, SubmissionView submission) {

		public static CreateSubmissionResult created(SubmissionView submission) {
			return new CreateSubmissionResult(true, false, null, submission);
		}

		public static CreateSubmissionResult notFound(String message) {
			return new CreateSubmissionResult(false, true, message, null);
		}
	}

	public record UpdateSubmissionResult(boolean updated, boolean notFound, String errorMessage, SubmissionView submission) {

		public static UpdateSubmissionResult updated(SubmissionView submission) {
			return new UpdateSubmissionResult(true, false, null, submission);
		}

		public static UpdateSubmissionResult notFound(String message) {
			return new UpdateSubmissionResult(false, true, message, null);
		}
	}
}
