package ru.dgorokhov.docservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dgorokhov.docservice.dal.Document;
import ru.dgorokhov.docservice.dal.RegistryEntry;
import ru.dgorokhov.docservice.dal.RegistryRepository;
import ru.dgorokhov.docservice.exception.ApprovalRegistryException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistryService {

    private final RegistryRepository registryRepository;

    @Transactional
    public RegistryEntry registerApproval(Document document, String approvedBy) {
        if (registryRepository.existsByDocumentId(document.getId()))
            throw new ApprovalRegistryException(document.getId(), "Already registered in approval registry");

        RegistryEntry registry = RegistryEntry.builder()
            .documentId(document.getId())
            .documentNumber(document.getNumber())
            .approvedBy(approvedBy)
            .build();
        registryRepository.save(registry);
        return registry;
    }

    @Transactional(readOnly = true)
    public boolean isRegistered(Long documentId) {
        return registryRepository.existsByDocumentId(documentId);
    }

    @Transactional(readOnly = true)
    public Optional<RegistryEntry> findByDocumentId(Long documentId) {
        return registryRepository.findByDocumentId(documentId);
    }

}
