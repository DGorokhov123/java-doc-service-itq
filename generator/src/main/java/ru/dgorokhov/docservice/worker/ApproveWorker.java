package ru.dgorokhov.docservice.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.dgorokhov.docservice.config.GeneratorConfig;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dal.DocumentRepository;
import ru.dgorokhov.docservice.dto.BatchApproveResponseDto;
import ru.dgorokhov.docservice.enums.DocumentStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApproveWorker {

    private final GeneratorConfig config;
    private final DocumentRepository documentRepository;

    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    @Scheduled(fixedRateString = "${app.generator.approve-interval-ms:5000}")
    public void processSubmittedDocuments() {
        log.info("ApproveWorker: Checking for SUBMITTED documents...");

        List<Document> submittedDocs = documentRepository.findByStatusForBatchProcessing(
                DocumentStatus.SUBMITTED,
                PageRequest.of(0, config.getBatchSize())
        );

        if (submittedDocs.isEmpty()) {
            log.info("ApproveWorker: No SUBMITTED documents to process");
            return;
        }

        long startTime = System.currentTimeMillis();
        List<Long> documentIds = submittedDocs.stream()
                .map(Document::getId)
                .toList();

        log.info("ApproveWorker: Processing {} SUBMITTED documents (batch size: {})", documentIds.size(), config.getBatchSize());

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("documentIds", documentIds);
            requestBody.put("initiator", "approve-worker");
            requestBody.put("comment", "Batch approve by worker");

            WebClient webClient = WebClient.builder().build();

            BatchApproveResponseDto responseDto = webClient.post()
                    .uri(config.getServiceUrl() + "/api/documents/approve")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(BatchApproveResponseDto.class)
                    .block();

            long successCount = responseDto != null ? responseDto.getSuccessCount() : 0;
            long failureCount = responseDto != null ? responseDto.getFailureCount() : 0;
            totalProcessed.addAndGet(documentIds.size());
            totalSuccess.addAndGet(successCount);

            log.info("ApproveWorker: Completed in {} ms. Processed: {}, Success: {}, Failure: {}",
                    System.currentTimeMillis() - startTime, documentIds.size(), successCount, failureCount);

        } catch (Exception e) {
            totalErrors.incrementAndGet();
            log.error("ApproveWorker: Error processing batch: {}", e.getMessage());
        }

        log.info("ApproveWorker: Total stats - Processed: {}, Success: {}, Errors: {}",
                totalProcessed.get(), totalSuccess.get(), totalErrors.get());
    }

}
