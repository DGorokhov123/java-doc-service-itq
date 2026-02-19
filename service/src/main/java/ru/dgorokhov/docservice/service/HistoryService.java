package ru.dgorokhov.docservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dal.History;
import ru.dgorokhov.docservice.dal.HistoryRepository;
import ru.dgorokhov.docservice.enums.HistoryAction;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    @Transactional
    public void logAction(Document document, HistoryAction action, String initiator, String comment) {
        History history = History.builder()
            .document(document)
            .action(action)
            .initiator(initiator)
            .comment(comment)
            .build();
        historyRepository.save(history);
        log.debug("Logged action {} for document {} by initiator {}", action, document.getNumber(), initiator);
    }

    @Transactional(readOnly = true)
    public List<History> getHistory(Long documentId) {
        return historyRepository.findByDocumentIdOrderByCreatedAtAsc(documentId);
    }

}
