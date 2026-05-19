package com.bspq26e8.backend.workspace.controller;

import com.bspq26e8.backend.codeexecution.CodeExecutionPreviewService;
import com.bspq26e8.backend.codeexecution.CodeExecutionPreviewService.PreviewExecutionCommand;
import com.bspq26e8.backend.codeexecution.CodeExecutionPreviewService.PreviewExecutionResult;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
public class WorkspaceController {

    private final CodeExecutionPreviewService codeExecutionPreviewService;

    public WorkspaceController(CodeExecutionPreviewService codeExecutionPreviewService) {
        this.codeExecutionPreviewService = codeExecutionPreviewService;
    }

    @PostMapping("/{problemId}/run")
    public ResponseEntity<?> runCode(
            @PathVariable UUID problemId,
            @RequestBody RunCodeRequest request
    ) {
        String sourceCode = firstNonBlank(request.sourceCode(), request.code());
        if (isBlank(sourceCode)) {
            return ResponseEntity.badRequest().body(error("sourceCode is required"));
        }
        if (request.languageId() == null && isBlank(request.languageCode()) && isBlank(request.language())) {
            return ResponseEntity.badRequest().body(error("languageId or language is required"));
        }

        PreviewExecutionCommand command = new PreviewExecutionCommand(
                problemId,
                request.languageId(),
                request.languageCode(),
                request.language(),
                sourceCode
        );
        PreviewExecutionResult result = codeExecutionPreviewService.runPreview(command);
        if (result.notFound()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(result.errorMessage()));
        }

        return ResponseEntity.ok(result.execution());
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first;
        }
        if (!isBlank(second)) {
            return second;
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, String> error(String message) {
        return Map.of("error", message);
    }

    public record RunCodeRequest(
            Long languageId,
            String languageCode,
            String language,
            String sourceCode,
            String code
    ) {
    }
}
