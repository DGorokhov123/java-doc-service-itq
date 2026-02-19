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
import ru.dgorokhov.docservice.dto.BatchSubmitResponseDto;
import ru.dgorokhov.docservice.enums.DocumentStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmitWorker {

    private final GeneratorConfig config;
    private final DocumentRepository documentRepository;

    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicLong totalSuccess = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);

    @Scheduled(fixedRateString = "${app.generator.submit-interval-ms:5000}")
    public void processDraftDocuments() {
        log.info("SubmitWorker: Checking for DRAFT documents...");

        List<Document> draftDocs = documentRepository.findByStatusForBatchProcessing(
                DocumentStatus.DRAFT,
                PageRequest.of(0, config.getBatchSize())
        );

        if (draftDocs.isEmpty()) {
            log.info("SubmitWorker: No DRAFT documents to process");
            return;
        }

        long startTime = System.currentTimeMillis();
        List<Long> documentIds = draftDocs.stream()
                .map(Document::getId)
                .toList();

        log.info("SubmitWorker: Processing {} DRAFT documents (batch size: {})", documentIds.size(), config.getBatchSize());

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("documentIds", documentIds);
            requestBody.put("initiator", "submit-worker");
            requestBody.put("comment", "Batch submit by worker");

            WebClient webClient = WebClient.builder().build();

            BatchSubmitResponseDto responseDto = webClient.post()
                    .uri(config.getServiceUrl() + "/api/documents/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(BatchSubmitResponseDto.class)
                    .block();

            long successCount = responseDto != null ? responseDto.getSuccessCount() : 0;
            long failureCount = responseDto != null ? responseDto.getFailureCount() : 0;
            totalProcessed.addAndGet(documentIds.size());
            totalSuccess.addAndGet(successCount);

            log.info("SubmitWorker: Completed in {} ms. Processed: {}, Success: {}, Failure: {}",
                    System.currentTimeMillis() - startTime, documentIds.size(), successCount, failureCount);

        } catch (Exception e) {
            totalErrors.incrementAndGet();
            log.error("SubmitWorker: Error processing batch: {}", e.getMessage());
        }

        log.info("SubmitWorker: Total stats - Processed: {}, Success: {}, Errors: {}",
                totalProcessed.get(), totalSuccess.get(), totalErrors.get());
    }

}
