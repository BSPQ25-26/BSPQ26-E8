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

	public Optional<SubmissionView> findById(UUID submissionId) {
		return submissionRepository.findById(submissionId).map(this::toView);
	}

	public Optional<SubmissionView> findBestMineByProblem(UUID userId, UUID problemId) {
		return submissionRepository.findBestMineByProblem(userId, problemId).map(this::toView);
	}

	public StartExecutionResult markExecutionStarted(UUID submissionId) {
		Optional<Submission> maybeSubmission = submissionRepository.findById(submissionId);
		if (maybeSubmission.isEmpty()) {
			return StartExecutionResult.notFound("Submission not found");
		}

		Submission submission = maybeSubmission.get();
		if (submission.getStatus() != SubmissionStatus.QUEUED) {
			return StartExecutionResult.skipped("Submission is not queued", toView(submission));
		}

		submission.markRunning();
		Submission saved = submissionRepository.save(submission);
		return StartExecutionResult.started(toView(saved));
	}

	public ApplyExecutionResultResult applyExecutionResult(ApplyExecutionResultCommand command) {
		Optional<Submission> maybeSubmission = submissionRepository.findById(command.submissionId());
		if (maybeSubmission.isEmpty()) {
			return ApplyExecutionResultResult.notFound("Submission not found");
		}

		Submission submission = maybeSubmission.get();
		submission.applyExecutionResult(
				command.status(),
				command.verdictMessage(),
				command.runtimeMs(),
				command.memoryMb(),
				command.testcasesPassed(),
				command.testcasesTotal()
		);

		Submission saved = submissionRepository.save(submission);
		return ApplyExecutionResultResult.updated(toView(saved));
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
				submission.getLanguage().getCode(),
				submission.getLanguage().getName(),
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
			String languageCode,
			String languageName,
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

	public record ApplyExecutionResultCommand(
			UUID submissionId,
			SubmissionStatus status,
			String verdictMessage,
			Integer runtimeMs,
			Integer memoryMb,
			int testcasesPassed,
			Integer testcasesTotal
	) {
	}

	public record StartExecutionResult(
			boolean started,
			boolean notFound,
			boolean skipped,
			String errorMessage,
			SubmissionView submission
	) {

		public static StartExecutionResult started(SubmissionView submission) {
			return new StartExecutionResult(true, false, false, null, submission);
		}

		public static StartExecutionResult notFound(String message) {
			return new StartExecutionResult(false, true, false, message, null);
		}

		public static StartExecutionResult skipped(String message, SubmissionView submission) {
			return new StartExecutionResult(false, false, true, message, submission);
		}
	}

	public record CreateSubmissionResult(boolean created, boolean notFound, String errorMessage, SubmissionView submission) {

		public static CreateSubmissionResult created(SubmissionView submission) {
			return new CreateSubmissionResult(true, false, null, submission);
		}

		public static CreateSubmissionResult notFound(String message) {
			return new CreateSubmissionResult(false, true, message, null);
		}
	}

	public record ApplyExecutionResultResult(
			boolean updated,
			boolean notFound,
			String errorMessage,
			SubmissionView submission
	) {

		public static ApplyExecutionResultResult updated(SubmissionView submission) {
			return new ApplyExecutionResultResult(true, false, null, submission);
		}

		public static ApplyExecutionResultResult notFound(String message) {
			return new ApplyExecutionResultResult(false, true, message, null);
		}
	}
}
