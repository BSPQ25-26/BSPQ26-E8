package com.bspq26e8.backend.problem.controller;

import com.bspq26e8.backend.common.AccessTokenService;
import com.bspq26e8.backend.problem.entity.ProblemDifficulty;
import com.bspq26e8.backend.problem.service.ProblemService;
import com.bspq26e8.backend.problem.service.ProblemService.CreateProblemCommand;
import com.bspq26e8.backend.problem.service.ProblemService.CreateProblemResult;
import com.bspq26e8.backend.problem.service.ProblemService.ProblemDetail;
import com.bspq26e8.backend.problem.service.ProblemService.ProblemSummary;
import com.bspq26e8.backend.problem.service.ProblemService.UpdateProblemCommand;
import com.bspq26e8.backend.problem.service.ProblemService.UpdateProblemResult;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

	private final ProblemService problemService;
	private final AccessTokenService accessTokenService;

	public ProblemController(ProblemService problemService, AccessTokenService accessTokenService) {
		this.problemService = problemService;
		this.accessTokenService = accessTokenService;
	}

	@PostMapping
	public ResponseEntity<?> createProblem(
			@RequestBody(required = false) CreateProblemRequest request,
			HttpServletRequest httpRequest
	) {
		if (request == null) {
			return ResponseEntity.badRequest().body(error("Request body is required"));
		}

		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		if (isBlank(request.title()) || isBlank(request.statementMd()) || isBlank(request.difficulty())) {
			return ResponseEntity.badRequest().body(error("title, statementMd and difficulty are required"));
		}

		Optional<ProblemDifficulty> difficulty = problemService.parseDifficulty(request.difficulty());
		if (difficulty.isEmpty()) {
			return ResponseEntity.badRequest().body(error("Invalid difficulty. Use EASY, MEDIUM or HARD"));
		}

		String title = request.title().trim();
		String slug = isBlank(request.slug()) ? slugify(title) : request.slug().trim().toLowerCase();
		if (!slug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
			return ResponseEntity.badRequest().body(error("Invalid slug format"));
		}

		CreateProblemCommand command = new CreateProblemCommand(
				slug,
				title,
				request.statementMd().trim(),
				normalizeRequiredText(request.inputSpecMd()),
				normalizeRequiredText(request.outputSpecMd()),
				normalizeRequiredText(request.constraintsMd()),
				normalizeOptionalText(request.hintsMd()),
				difficulty.get(),
				authenticatedUserId.get(),
				normalizeOptionalText(request.solutionTemplate()),
				normalizeJsonConfig(request.languageCompilationConfig())
		);

		CreateProblemResult result = problemService.createProblem(command);
		if (result.notFound()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(result.errorMessage()));
		}
		if (!result.created()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error(result.errorMessage()));
		}

		return ResponseEntity.status(HttpStatus.CREATED).body(result.problem());
	}

	@GetMapping
	public ResponseEntity<?> listPublicProblems(
			@RequestParam(required = false) String language,
			@RequestParam(required = false) String difficulty,
			@RequestParam(required = false) UUID authorId,
		    @RequestParam(required = false) String author,
			@RequestParam(required = false, name = "name") String name
	) {
		if (!isBlank(difficulty) && problemService.parseDifficulty(difficulty).isEmpty()) {
			return ResponseEntity.badRequest().body(error("Invalid difficulty. Use EASY, MEDIUM or HARD"));
		}

		List<ProblemSummary> problems = problemService.listPublicProblems(language, difficulty, authorId, author, name);
		return ResponseEntity.ok(problems);
	}

	@DeleteMapping("/{problemId}")
	public ResponseEntity<?> deleteProblem(
			@PathVariable UUID problemId,
			HttpServletRequest httpRequest
	) {
		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		Optional<ProblemService.DeleteProblemError> error = problemService.deleteProblemByAuthor(problemId, authenticatedUserId.get());
		if (error.isPresent()) {
			if (error.get() == ProblemService.DeleteProblemError.FORBIDDEN) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only the author can delete this problem"));
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Problem not found"));
		}

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{problemId}")
	public ResponseEntity<?> getProblemDetail(@PathVariable UUID problemId) {
		Optional<ProblemDetail> detail = problemService.getProblemDetail(problemId);
		if (detail.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("Problem not found"));
		}
		return ResponseEntity.ok(detail.get());
	}

	@GetMapping("/by-author/{authorId}")
	public ResponseEntity<?> listProblemsByAuthor(@PathVariable UUID authorId) {
		List<ProblemSummary> problems = problemService.listProblemsByAuthor(authorId);
		return ResponseEntity.ok(problems);
	}

	@PutMapping("/{problemId}")
	public ResponseEntity<?> updateProblemByAuthor(
			@PathVariable UUID problemId,
			@RequestBody(required = false) UpdateProblemRequest request,
			HttpServletRequest httpRequest
	) {
		if (request == null) {
			return ResponseEntity.badRequest().body(error("Request body is required"));
		}

		Optional<UUID> authenticatedUserId = authenticatedUserId(httpRequest);
		if (authenticatedUserId.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Missing or invalid access token"));
		}

		if (!isBlank(request.difficulty()) && problemService.parseDifficulty(request.difficulty()).isEmpty()) {
			return ResponseEntity.badRequest().body(error("Invalid difficulty. Use EASY, MEDIUM or HARD"));
		}

		String normalizedSlug = normalizeOptionalText(request.slug());
		if (normalizedSlug != null) {
			normalizedSlug = normalizedSlug.toLowerCase();
			if (!normalizedSlug.matches("^[a-z0-9]+(?:-[a-z0-9]+)*$")) {
				return ResponseEntity.badRequest().body(error("Invalid slug format"));
			}
		}

		UpdateProblemCommand command = new UpdateProblemCommand(
				authenticatedUserId.get(),
				normalizedSlug,
				normalizeOptionalText(request.title()),
				normalizeOptionalText(request.statementMd()),
				normalizeOptionalText(request.inputSpecMd()),
				normalizeOptionalText(request.outputSpecMd()),
				normalizeOptionalText(request.constraintsMd()),
				normalizeOptionalText(request.hintsMd()),
				problemService.parseDifficulty(request.difficulty()).orElse(null),
				normalizeOptionalText(request.solutionTemplate()),
				normalizeOptionalText(request.languageCompilationConfig())
		);

		UpdateProblemResult result = problemService.updateProblemByAuthor(problemId, command);
		if (result.notFound()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(result.errorMessage()));
		}
		if (result.forbidden()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(result.errorMessage()));
		}
		if (!result.updated()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(error(result.errorMessage()));
		}

		return ResponseEntity.ok(result.problem());
	}

	private String slugify(String input) {
		String slug = input.toLowerCase()
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("^-+|-+$", "");

		return slug.isBlank() ? "problem" : slug;
	}

	private String normalizeRequiredText(String value) {
		return value == null ? "" : value.trim();
	}

	private String normalizeOptionalText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private String normalizeJsonConfig(String value) {
		if (value == null || value.isBlank()) {
			return "{}";
		}
		return value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private Map<String, String> error(String message) {
		return Map.of("error", message);
	}

	private Optional<UUID> authenticatedUserId(HttpServletRequest httpRequest) {
		String authorization = httpRequest.getHeader("Authorization");
		return accessTokenService.extractUserIdFromAuthorizationHeader(authorization);
	}

	public record CreateProblemRequest(
			String slug,
			String title,
			String statementMd,
			String inputSpecMd,
			String outputSpecMd,
			String constraintsMd,
			String hintsMd,
			String difficulty,
			String solutionTemplate,
			String languageCompilationConfig
	) {
	}

	public record UpdateProblemRequest(
			String slug,
			String title,
			String statementMd,
			String inputSpecMd,
			String outputSpecMd,
			String constraintsMd,
			String hintsMd,
			String difficulty,
			String solutionTemplate,
			String languageCompilationConfig
	) {
	}
}
