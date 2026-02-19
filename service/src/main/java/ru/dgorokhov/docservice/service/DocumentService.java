package ru.dgorokhov.docservice.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dal.DocumentRepository;
import ru.dgorokhov.docservice.dal.DocumentSpecifications;
import ru.dgorokhov.docservice.dto.ApproveResult;
import ru.dgorokhov.docservice.dto.SubmitResult;
import ru.dgorokhov.docservice.enums.ApproveResultStatus;
import ru.dgorokhov.docservice.enums.DocumentStatus;
import ru.dgorokhov.docservice.enums.HistoryAction;
import ru.dgorokhov.docservice.enums.SubmitResultStatus;
import ru.dgorokhov.docservice.exception.DocumentNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final HistoryService historyService;
    private final RegistryService registryService;

    private final AtomicInteger documentCounter = new AtomicInteger(0);

    private final TransactionTemplate transactionTemplate;
    private final EntityManager entityManager;

    @Transactional
    public Document createDocument(String author, String title) {
        int count = documentCounter.incrementAndGet();
        String number = "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + count;
        Document document = Document.builder()
                .number(number)
                .author(author)
                .title(title)
                .status(DocumentStatus.DRAFT)
                .build();
        documentRepository.save(document);
        log.debug("Created document {} with number {}", document.getId(), number);
        return document;
    }

    @Transactional(readOnly = true)
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Not found document " + id));
    }

    @Transactional(readOnly = true)
    public List<Document> getDocuments(List<Long> ids) {
        return documentRepository.findByIdIn(ids);
    }

    @Transactional(readOnly = true)
    public Page<Document> getDocumentsPaged(List<Long> ids, int page, int size, String sortBy, boolean ascending) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return documentRepository.findByIdIn(ids, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Document> searchDocuments(
            DocumentStatus status,
            String author,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            boolean updated,
            int page,
            int size,
            String sortBy,
            boolean ascending
    ) {
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Document> spec = DocumentSpecifications.buildSearchCriteria(status, author, dateFrom, dateTo, updated);
        return documentRepository.findAll(spec, pageable);
    }

    public SubmitResult submitDocument(Long id, String initiator, String comment) {
        try {
            return transactionTemplate.execute(status -> {
                Optional<Document> optionalDocument = documentRepository.findById(id);
                if (optionalDocument.isEmpty())
                    return new SubmitResult(id, false, SubmitResultStatus.NOT_FOUND, "Not found document " + id);
                Document document = optionalDocument.get();

                if (document.getStatus() != DocumentStatus.DRAFT)
                    return new SubmitResult(id, false, SubmitResultStatus.CONFLICT,
                            "Cannot submit document with status " + document.getStatus());

                document.setStatus(DocumentStatus.SUBMITTED);
                documentRepository.save(document);
                historyService.logAction(document, HistoryAction.SUBMIT, initiator, comment);
                log.debug("Document {} submitted by {}", document.getNumber(), initiator);
                return new SubmitResult(id, true, SubmitResultStatus.SUCCESS, "Document submitted successfully");
            });
        } catch (Exception e) {
            log.error("Failed to submit document {}: {}", id, e.getMessage());
            return new SubmitResult(id, false, SubmitResultStatus.ERROR, "Failed to submit document: " + e.getMessage());
        }
    }

    public List<SubmitResult> submitDocuments(List<Long> ids, String initiator, String comment) {
        List<SubmitResult> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                results.add(submitDocument(id, initiator, comment));
            } catch (Exception e) {
                log.error("Error submitting document {}: {}", id, e.getMessage());
                results.add(new SubmitResult(id, false, SubmitResultStatus.ERROR, e.getMessage()));
            }
        }
        return results;
    }

    public ApproveResult approveDocument(Long id, String initiator, String comment) {
        try {
            return transactionTemplate.execute(status -> {
                Optional<Document> optionalDocument = documentRepository.findById(id);
                if (optionalDocument.isEmpty())
                    return new ApproveResult(id, false, ApproveResultStatus.NOT_FOUND, "Not found document " + id);
                Document document = optionalDocument.get();

                if (document.getStatus() != DocumentStatus.SUBMITTED)
                    return new ApproveResult(id, false, ApproveResultStatus.CONFLICT,
                            "Cannot approve document with status " + document.getStatus());

                document.setStatus(DocumentStatus.APPROVED);
                documentRepository.save(document);
                historyService.logAction(document, HistoryAction.APPROVE, initiator, comment);
                registryService.registerApproval(document, initiator);
                log.debug("Document {} approved by {}", document.getNumber(), initiator);
                return new ApproveResult(id, true, ApproveResultStatus.SUCCESS, "Document approved successfully");
            });
        } catch (Exception e) {
            log.error("Failed to register approval {}: {}", id, e.getMessage());
            return new ApproveResult(id, false, ApproveResultStatus.REGISTER_ERROR, "Failed to register approval: " + e.getMessage());
        }
    }

    public List<ApproveResult> approveDocuments(List<Long> ids, String initiator, String comment) {
        List<ApproveResult> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                results.add(approveDocument(id, initiator, comment));
            } catch (Exception e) {
                log.error("Error approving document {}: {}", id, e.getMessage());
                results.add(new ApproveResult(id, false, ApproveResultStatus.ERROR, e.getMessage()));
            }
        }
        return results;
    }

}
