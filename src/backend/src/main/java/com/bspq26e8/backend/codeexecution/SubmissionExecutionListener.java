package com.bspq26e8.backend.codeexecution;

import com.bspq26e8.backend.submission.repository.SubmissionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SubmissionExecutionListener {

    private final SubmissionRepository submissionRepository;
    private final CodeRunner codeRunner;
    private final ApplicationEventPublisher eventPublisher;

    public SubmissionExecutionListener(
            SubmissionRepository submissionRepository,
            CodeRunner codeRunner,
            ApplicationEventPublisher eventPublisher
    ) {
        this.submissionRepository = submissionRepository;
        this.codeRunner = codeRunner;
        this.eventPublisher = eventPublisher;
    }

    // Scaffold only (no listener methods yet).
}
