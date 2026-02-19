package ru.dgorokhov.docservice.service;

import jakarta.persistence.EntityManager;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dto.ApproveResult;
import ru.dgorokhov.docservice.dto.ConcurrentApproveResponseDto;

import java.util.concurrent.CountDownLatch;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcurrentApproveTester {

    private final DocumentService documentService;
    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;

    public ConcurrentApproveResponseDto doApproveTest(int threads, int attempts) {
        Document document = documentService.createDocument("author", "title");
        Long documentId = document.getId();
        documentService.submitDocument(documentId, "initiator", "comment");

        ConcurrentApproveStats stats = new ConcurrentApproveStats();
        Thread[] threadArr = new Thread[threads];
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            int finalI = i;
            threadArr[i] = new Thread(() -> {
                for (int j = 0; j < attempts; j++) {
                    try {
                        ApproveResult result = documentService.approveDocument(
                                documentId,
                                "thread " + finalI,
                                "attempt " + j
                        );

                        synchronized (stats) {
                            if (result.success()) {
                                stats.successfulApprovals++;
                            } else {
                                switch (result.status()) {
                                    case CONFLICT -> stats.conflicts++;
                                    case NOT_FOUND -> stats.notFoundErrors++;
                                    case REGISTER_ERROR -> stats.registerErrors++;
                                    default -> stats.otherErrors++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        synchronized (stats) {
                            stats.otherErrors++;
                            System.out.println("===========");
                        }
                    }
                }
                latch.countDown();
            });
            threadArr[i].start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Mass approve test is interrupted: " + e);
        }

        Document updatedDocument = transactionTemplate.execute(status -> {
            entityManager.clear();
            return documentService.getDocument(documentId);
        });

        return ConcurrentApproveResponseDto.builder()
                .documentId(documentId)
                .documentNumber(updatedDocument.getNumber())
                .finalStatus(updatedDocument.getStatus().name())
                .totalAttempts(threads * attempts)
                .successfulApprovals(stats.successfulApprovals)
                .conflicts(stats.conflicts)
                .notFoundErrors(stats.notFoundErrors)
                .registerErrors(stats.registerErrors)
                .otherErrors(stats.otherErrors)
                .build();
    }

    @NoArgsConstructor
    public static class ConcurrentApproveStats {
        public int successfulApprovals = 0;
        public int conflicts = 0;
        public int notFoundErrors = 0;
        public int registerErrors = 0;
        public int otherErrors = 0;
    }


}
