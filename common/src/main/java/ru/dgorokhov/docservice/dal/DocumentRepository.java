package ru.dgorokhov.docservice.dal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dgorokhov.docservice.enums.DocumentStatus;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    List<Document> findByIdIn(List<Long> ids);

    Page<Document> findByIdIn(List<Long> ids, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.status = :status ORDER BY d.createdAt ASC")
    List<Document> findByStatusForBatchProcessing(
            @Param("status") DocumentStatus status,
            Pageable pageable
    );

}
