package com.bspq26e8.backend.codeexecution;

import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "codeexecution.queue", name = "enabled", havingValue = "true")
public class CodeExecutionQueueWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeExecutionQueueWorker.class);

    private final CodeExecutionService codeExecutionService;
    private final int batchSize;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CodeExecutionQueueWorker(
            CodeExecutionService codeExecutionService,
            @Value("${codeexecution.queue.batch-size:5}") int batchSize
    ) {
        this.codeExecutionService = codeExecutionService;
        this.batchSize = Math.max(1, batchSize);
    }

    @Scheduled(fixedDelayString = "${codeexecution.queue.poll-delay-ms:5000}")
    public void processQueue() {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            CodeExecutionService.QueueProcessingResult result = codeExecutionService.processQueuedSubmissions(batchSize);
            if (result.total() > 0) {
                LOGGER.info(
                        "Processed submission execution queue: total={}, processed={}, failed={}, skipped={}",
                        result.total(),
                        result.processed(),
                        result.failed(),
                        result.skipped()
                );
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to process submission execution queue", ex);
        } finally {
            running.set(false);
        }
    }
}
