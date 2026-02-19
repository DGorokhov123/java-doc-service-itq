package ru.dgorokhov.docservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.dgorokhov.docservice.api.DocumentApi;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dto.*;
import ru.dgorokhov.docservice.enums.DocumentStatus;
import ru.dgorokhov.docservice.mapper.BatchMapper;
import ru.dgorokhov.docservice.mapper.DocumentMapper;
import ru.dgorokhov.docservice.mapper.HistoryMapper;
import ru.dgorokhov.docservice.service.ConcurrentApproveTester;
import ru.dgorokhov.docservice.service.DocumentService;
import ru.dgorokhov.docservice.service.HistoryService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class DocumentController implements DocumentApi {

    private final DocumentService documentService;
    private final HistoryService historyService;
    private final ConcurrentApproveTester concurrentApproveTester;

    @Override
    public DocumentResponseDto createDocument(CreateDocumentRequestDto request) {
        log.debug("Creating document for author: {}", request.getAuthor());
        Document document = documentService.createDocument(request.getAuthor(), request.getTitle());
        return DocumentMapper.toDto(document);
    }

    @Override
    public DocumentResponseDto getDocument(Long id) {
        log.debug("Getting document: {}", id);
        Document document = documentService.getDocument(id);
        List<HistoryEntryResponseDto> history = historyService.getHistory(id).stream()
                .map(HistoryMapper::toDto)
                .toList();
        return DocumentMapper.toDto(document, history);
    }

    @Override
    public List<DocumentResponseDto> getDocumentsBatch(List<Long> ids) {
        log.debug("Getting documents batch: {}", ids);
        List<Document> documents = documentService.getDocuments(ids);
        return documents.stream()
                .map(DocumentMapper::toDto)
                .toList();
    }

    @Override
    public PaginatedResponseDto<DocumentResponseDto> getDocumentsBatchPaged(BatchDocumentRequestDto request) {
        log.debug("Getting documents batch paged: {}", request.getDocumentIds());
        Page<Document> page = documentService.getDocumentsPaged(
                request.getDocumentIds(),
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20,
                request.getSortBy() != null ? request.getSortBy() : "id",
                request.getAscending() != null && request.getAscending()
        );
        Page<DocumentResponseDto> responsePage = page.map(DocumentMapper::toDto);
        return PaginatedResponseDto.fromPage(responsePage);
    }

    @Override
    public PaginatedResponseDto<DocumentResponseDto> searchDocuments(
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
        if (sortBy == null || sortBy.isBlank()) sortBy = updated ? "updatedAt" : "createdAt";
        log.debug("Searching documents with filters: {}, {}, {}, {}, {}, {}, {}, {}, {}",
                status, author, dateFrom, dateTo, updated, page, size, sortBy, ascending);
        Page<Document> documentPage = documentService.searchDocuments(
                status, author, dateFrom, dateTo, updated, page, size, sortBy, ascending);
        Page<DocumentResponseDto> responsePage = documentPage.map(DocumentMapper::toDto);
        return PaginatedResponseDto.fromPage(responsePage);
    }

    @Override
    public BatchSubmitResponseDto submitDocuments(BatchDocumentRequestDto request) {
        log.debug("Submitting documents: {} by initiator: {}", request.getDocumentIds(), request.getInitiator());
        List<SubmitResult> results = documentService.submitDocuments(
                request.getDocumentIds(),
                request.getInitiator(),
                request.getComment()
        );
        return BatchMapper.toSubmitDto(results);
    }

    @Override
    public BatchApproveResponseDto approveDocuments(BatchDocumentRequestDto request) {
        log.debug("Approving documents: {} by initiator: {}", request.getDocumentIds(), request.getInitiator());
        List<ApproveResult> results = documentService.approveDocuments(
                request.getDocumentIds(),
                request.getInitiator(),
                request.getComment()
        );
        return BatchMapper.toApproveDto(results);
    }

    @Override
    public ConcurrentApproveResponseDto concurrentApprove(int threads, int attempts) {
        log.debug("Starting concurrent approve test. threads: {}, attempts: {}", threads, attempts);
        return concurrentApproveTester.doApproveTest(threads, attempts);
    }

}
