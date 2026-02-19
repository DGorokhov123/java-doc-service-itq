package ru.dgorokhov.docservice.api;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.dgorokhov.docservice.dto.*;
import ru.dgorokhov.docservice.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentApi {

    @PostMapping("/api/documents")
    @ResponseStatus(HttpStatus.OK)
    public DocumentResponseDto createDocument(
            @Valid @RequestBody CreateDocumentRequestDto request
    );

    @GetMapping("/api/documents/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DocumentResponseDto getDocument(
            @PathVariable Long id
    );

    @PostMapping("/api/documents/batch")
    @ResponseStatus(HttpStatus.OK)
    public List<DocumentResponseDto> getDocumentsBatch(
            @RequestBody List<Long> ids
    );

    @PostMapping("/api/documents/batch/paged")
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResponseDto<DocumentResponseDto> getDocumentsBatchPaged(
            @RequestBody BatchDocumentRequestDto request
    );

    @GetMapping("/api/documents/search")
    @ResponseStatus(HttpStatus.OK)
    public PaginatedResponseDto<DocumentResponseDto> searchDocuments(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "false") boolean updated,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending
    );

    @PostMapping("/api/documents/submit")
    @ResponseStatus(HttpStatus.OK)
    public BatchSubmitResponseDto submitDocuments(
            @Valid @RequestBody BatchDocumentRequestDto request
    );

    @PostMapping("/api/documents/approve")
    @ResponseStatus(HttpStatus.OK)
    public BatchApproveResponseDto approveDocuments(
            @Valid @RequestBody BatchDocumentRequestDto request
    );

    @PostMapping("/api/documents/concurrent-approve")
    @ResponseStatus(HttpStatus.OK)
    public ConcurrentApproveResponseDto concurrentApprove(
            @RequestParam(defaultValue = "5") int threads,
            @RequestParam(defaultValue = "10") int attempts
    );

}
