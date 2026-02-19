package ru.dgorokhov.docservice.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.dgorokhov.docservice.config.GeneratorConfig;
import ru.dgorokhov.docservice.dto.CreateDocumentRequestDto;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentGeneratorUtil implements CommandLineRunner {

    private final GeneratorConfig config;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting document generator. Target: {} documents, batch size: {}",
                config.getDocumentsCount(), config.getBatchSize());

        AtomicInteger created = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();
        WebClient webClient = WebClient.builder().build();

        for (int i = 0; i < config.getDocumentsCount(); i++) {
            try {
                CreateDocumentRequestDto request = new CreateDocumentRequestDto(
                        "generator-user-" + (i % 10),
                        "Generated Document #" + (i + 1)
                );

                webClient.post()
                        .uri(config.getServiceUrl() + "/api/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                int current = created.incrementAndGet();

                if (current % config.getBatchSize() == 0 || current == config.getDocumentsCount())
                    log.info("Generation progress: {}/{} documents created", current, config.getDocumentsCount());

            } catch (Exception e) {
                errors.incrementAndGet();
                log.error("Error creating document {}: {}", i + 1, e.getMessage());
            }
        }

        log.info("Document generation completed in {} ms", System.currentTimeMillis() - startTime);
        log.info("Created={}, errors={}", created.get(), errors.get());
    }

}
