package ru.dgorokhov.docservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dal.History;
import ru.dgorokhov.docservice.dal.RegistryEntry;
import ru.dgorokhov.docservice.dto.ApproveResult;
import ru.dgorokhov.docservice.dto.SubmitResult;
import ru.dgorokhov.docservice.enums.ApproveResultStatus;
import ru.dgorokhov.docservice.enums.DocumentStatus;
import ru.dgorokhov.docservice.enums.HistoryAction;
import ru.dgorokhov.docservice.enums.SubmitResultStatus;
import ru.dgorokhov.docservice.service.DocumentService;
import ru.dgorokhov.docservice.service.HistoryService;
import ru.dgorokhov.docservice.service.RegistryService;

import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RegistryService registryService;

    @Test
    void testCreateDocument() {
        Document document = documentService.createDocument("test-author", "Test Document");
        assertNotNull(document);
        assertNotNull(document.getId());
        assertEquals("test-author", document.getAuthor());
        assertEquals("Test Document", document.getTitle());
        assertEquals(DocumentStatus.DRAFT, document.getStatus());
        assertNotNull(document.getNumber());
    }

    @Test
    void testGetDocument() {
        Document created = documentService.createDocument("test-author", "Test Document");
        Document found = documentService.getDocument(created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getNumber(), found.getNumber());
    }

    @Test
    void testSubmitDocument_Success() {
        Document document = documentService.createDocument("test-author", "Test Document");
        SubmitResult result = documentService.submitDocument(document.getId(), "initiator-1", "Test comment");
        assertTrue(result.success());
        assertEquals(SubmitResultStatus.SUCCESS, result.status());

        Document updated = documentService.getDocument(document.getId());
        assertEquals(DocumentStatus.SUBMITTED, updated.getStatus());

        List<History> history = historyService.getHistory(document.getId());
        assertEquals(1, history.size());
        assertEquals(HistoryAction.SUBMIT, history.getFirst().getAction());
        assertEquals("initiator-1", history.getFirst().getInitiator());
    }

    @Test
    void testSubmitDocument_Conflict() {
        Document document = documentService.createDocument("test-author", "Test Document");
        documentService.submitDocument(document.getId(), "initiator-1", "First submit");
        SubmitResult result = documentService.submitDocument(document.getId(), "initiator-2", "Second submit");
        assertFalse(result.success());
        assertEquals(SubmitResultStatus.CONFLICT, result.status());
    }

    @Test
    void testApproveDocument_Success() {
        Document document = documentService.createDocument("test-author", "Test Document");
        documentService.submitDocument(document.getId(), "initiator-1", "Submit comment");
        ApproveResult result = documentService.approveDocument(document.getId(), "approver-1", "Approve comment");
        assertTrue(result.success());
        assertEquals(ApproveResultStatus.SUCCESS, result.status());

        Document updated = documentService.getDocument(document.getId());
        assertEquals(DocumentStatus.APPROVED, updated.getStatus());

        List<History> history = historyService.getHistory(document.getId());
        assertEquals(2, history.size());
        assertEquals(HistoryAction.APPROVE, history.get(1).getAction());

        RegistryEntry registry = registryService.findByDocumentId(document.getId()).orElseThrow();
        assertNotNull(registry);
        assertEquals("approver-1", registry.getApprovedBy());
    }

    @Test
    void testApproveDocument_Conflict() {
        Document document = documentService.createDocument("test-author", "Test Document");
        ApproveResult result = documentService.approveDocument(document.getId(), "approver-1", "Approve comment");
        assertFalse(result.success());
        assertEquals(ApproveResultStatus.CONFLICT, result.status());
    }

    @Test
    void testBatchSubmit() {
        Document doc1 = documentService.createDocument("author-1", "Document 1");
        Document doc2 = documentService.createDocument("author-2", "Document 2");
        Document doc3 = documentService.createDocument("author-3", "Document 3");

        List<Long> ids = List.of(doc1.getId(), doc2.getId(), doc3.getId());
        List<SubmitResult> results = documentService.submitDocuments(ids, "batch-initiator", "Batch submit");
        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(SubmitResult::success));

        for (Long id : ids) {
            Document doc = documentService.getDocument(id);
            assertEquals(DocumentStatus.SUBMITTED, doc.getStatus());
        }
    }

    @Test
    void testBatchApprove_WithPartialResults() {
        Document doc1 = documentService.createDocument("author-1", "Document 1");
        Document doc2 = documentService.createDocument("author-2", "Document 2");
        Document doc3 = documentService.createDocument("author-3", "Document 3");
        documentService.submitDocument(doc1.getId(), "initiator", "Submit");
        documentService.submitDocument(doc2.getId(), "initiator", "Submit");
        documentService.submitDocument(doc3.getId(), "initiator", "Submit");

        List<Long> ids = List.of(doc1.getId(), doc2.getId(), 999L, doc3.getId());
        List<ApproveResult> results = documentService.approveDocuments(ids, "approver", "Approve");
        assertEquals(4, results.size());
        assertTrue(results.get(0).success());
        assertTrue(results.get(1).success());
        assertFalse(results.get(2).success());
        assertEquals(ApproveResultStatus.NOT_FOUND, results.get(2).status());
        assertTrue(results.get(3).success());
    }

    @Test
    void testSearchDocuments() {
        documentService.createDocument("author-1", "Document 1");
        documentService.createDocument("author-1", "Document 2");
        documentService.createDocument("author-2", "Document 3");
        // searchDocuments( status, author, dateFrom, dateTo, updated, page, size, sortBy, ascending )
        var page = documentService.searchDocuments(null, "author-1", null, null, false, 0, 10, "createdAt", false);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void testApproveDocument_RegisterError() {
        Document document = documentService.createDocument("test-author", "Test Document");
        SubmitResult submitResult = documentService.submitDocument(document.getId(), "submitter-1", "comment");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<ApproveResult> task1 = () -> documentService.approveDocument(document.getId(), "approver-1", "comment");
        Callable<ApproveResult> task2 = () -> documentService.approveDocument(document.getId(), "approver-2", "comment");
        Future<ApproveResult> future1 = executor.submit(task1);
        Future<ApproveResult> future2 = executor.submit(task2);

        try {
            ApproveResult result1 = future1.get();
            ApproveResult result2 = future2.get();

            assertTrue(result1.success() || result2.success());
            assertFalse(result1.success() && result2.success());
            assertTrue(result1.status() == ApproveResultStatus.REGISTER_ERROR || result2.status() == ApproveResultStatus.REGISTER_ERROR);

        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
