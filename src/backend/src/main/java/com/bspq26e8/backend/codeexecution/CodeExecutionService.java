package com.bspq26e8.backend.codeexecution;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CodeExecutionService {

    private final ApplicationEventPublisher eventPublisher;

    public CodeExecutionService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
