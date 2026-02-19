package ru.dgorokhov.docservice.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistryRepository extends JpaRepository<ru.dgorokhov.docservice.dal.RegistryEntry, Long> {

    Optional<ru.dgorokhov.docservice.dal.RegistryEntry> findByDocumentId(Long documentId);

    boolean existsByDocumentId(Long documentId);

}
