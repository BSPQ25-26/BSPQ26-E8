package com.bspq26e8.backend.submission.controller;

import com.bspq26e8.backend.auth.security.AccessTokenService;
import com.bspq26e8.backend.submission.service.SubmissionService;
import com.bspq26e8.backend.submission.service.SubmissionService.CreateSubmissionCommand;
import com.bspq26e8.backend.submission.service.SubmissionService.CreateSubmissionResult;
import com.bspq26e8.backend.submission.service.SubmissionService.SubmissionView;
import com.bspq26e8.backend.submission.service.SubmissionService.UpdateSubmissionCommand;
import com.bspq26e8.backend.submission.service.SubmissionService.UpdateSubmissionResult;
import com.bspq26e8.backend.submission.service.SubmissionService.ResubmitCommand;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

	private final SubmissionService submissionService;
	private final AccessTokenService accessTokenService;

	public SubmissionController(SubmissionService submissionService, AccessTokenService accessTokenService) {
		this.submissionService = submissionService;
		this.accessTokenService = accessTokenService;
	}

	@PostMapping
	public ResponseEntity<?> createSubmission(
			@RequestBody(required = false) CreateSubmissionRequest request,
			HttpServletRequest httpRequest
	) {
		if (request == null) {
			return ResponseEntity.badRequest().body(error("Request body is required"));
		}

		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		if (request.problemId() == null || request.languageId() == null || isBlank(request.sourceCode())) {
			return ResponseEntity.badRequest().body(error("problemId, languageId and sourceCode are required"));
		}

		CreateSubmissionCommand command = new CreateSubmissionCommand(
				authenticatedUserId.get(),
				request.problemId(),
				request.languageId(),
				request.sourceCode().trim()
		);

		CreateSubmissionResult result = submissionService.createSubmission(command);
		if (result.notFound()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(result.errorMessage()));
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(result.submission());
	}

	@GetMapping("/mine")
	public ResponseEntity<?> listMine(
			@RequestParam(required = false) UUID problemId,
			@RequestParam(required = false) String status,
			HttpServletRequest httpRequest
	) {
		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		if (!isBlank(status) && submissionService.parseStatus(status).isEmpty()) {
			return ResponseEntity.badRequest().body(error("Invalid status"));
		}

		List<SubmissionView> submissions = submissionService.listMine(authenticatedUserId.get(), problemId, status);
		return ResponseEntity.ok(submissions);
	}

	@GetMapping("/problem/{problemId}")
	public ResponseEntity<?> listByProblem(
			@PathVariable UUID problemId,
			@RequestParam(required = false) String status
	) {
		if (!isBlank(status) && submissionService.parseStatus(status).isEmpty()) {
			return ResponseEntity.badRequest().body(error("Invalid status"));
		}

		List<SubmissionView> submissions = submissionService.listByProblem(problemId, status);
		return ResponseEntity.ok(submissions);
	}

	@GetMapping("/mine/latest")
	public ResponseEntity<?> latestMine(
			@RequestParam(required = false) UUID problemId,
			HttpServletRequest httpRequest
	) {
		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		Optional<SubmissionView> latest = submissionService.findLatestMine(authenticatedUserId.get(), problemId);
		if (latest.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("No submissions found"));
		}

		return ResponseEntity.ok(latest.get());
	}

	@GetMapping("/problem/{problemId}/best")
	public ResponseEntity<?> bestByProblem(
			@PathVariable UUID problemId,
			HttpServletRequest httpRequest
	) {
		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		Optional<SubmissionView> best = submissionService.findBestMineByProblem(authenticatedUserId.get(), problemId);
		if (best.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("No submissions found"));
		}

		return ResponseEntity.ok(best.get());
	}

	@PutMapping("/{submissionId}")
	public ResponseEntity<?> updateSubmission(
			@PathVariable UUID submissionId,
			@RequestBody(required = false) UpdateSubmissionRequest request,
			HttpServletRequest httpRequest
	) {
		if (request == null) {
			return ResponseEntity.badRequest().body(error("Request body is required"));
		}

		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		if (isBlank(request.sourceCode()) && request.languageId() == null) {
			return ResponseEntity.badRequest().body(error("At least one of sourceCode or languageId is required"));
		}

		UpdateSubmissionCommand command = new UpdateSubmissionCommand(
				submissionId,
				authenticatedUserId.get(),
				request.languageId(),
				normalizeOptional(request.sourceCode())
		);

		UpdateSubmissionResult result = submissionService.updateSubmission(command);
		if (result.notFound()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(result.errorMessage()));
		}

		return ResponseEntity.ok(result.submission());
	}

	@PostMapping("/{submissionId}/resubmit")
	public ResponseEntity<?> resubmit(
			@PathVariable UUID submissionId,
			@RequestBody(required = false) ResubmitRequest request,
			HttpServletRequest httpRequest
	) {
		if (request == null) {
			return ResponseEntity.badRequest().body(error("Request body is required"));
		}

		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		ResubmitCommand command = new ResubmitCommand(
				submissionId,
				authenticatedUserId.get(),
				request.languageId(),
				normalizeOptional(request.sourceCode())
		);

		CreateSubmissionResult result = submissionService.resubmit(command);
		if (result.notFound()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(result.errorMessage()));
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(result.submission());
	}

	@DeleteMapping("/{submissionId}")
	public ResponseEntity<?> deleteSubmission(
			@PathVariable UUID submissionId,
			HttpServletRequest httpRequest
	) {
		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		Optional<String> error = submissionService.deleteSubmission(submissionId, authenticatedUserId.get());
		if (error.isPresent()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", error.get()));
		}

		return ResponseEntity.noContent().build();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String normalizeOptional(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isBlank() ? null : trimmed;
	}

	private Map<String, String> error(String message) {
		return Map.of("error", message);
	}

	private Optional<UUID> authenticatedUserId(HttpServletRequest httpRequest) {
		String authorization = httpRequest.getHeader("Authorization");
		return accessTokenService.extractUserIdFromAuthorizationHeader(authorization);
	}

	public record CreateSubmissionRequest(UUID problemId, Long languageId, String sourceCode) {
	}

	public record UpdateSubmissionRequest(Long languageId, String sourceCode) {
	}

	public record ResubmitRequest(Long languageId, String sourceCode) {
	}
}
