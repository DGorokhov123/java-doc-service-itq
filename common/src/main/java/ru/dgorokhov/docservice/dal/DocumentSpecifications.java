package ru.dgorokhov.docservice.dal;

import org.springframework.data.jpa.domain.Specification;
import ru.dgorokhov.docservice.enums.DocumentStatus;

import java.time.LocalDateTime;

public class DocumentSpecifications {

    public static Specification<Document> hasStatus(DocumentStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Document> hasAuthor(String author) {
        return (root, query, cb) -> author != null ? cb.equal(root.get("author"), author) : null;
    }

    public static Specification<Document> createdAtAfter(LocalDateTime dateFrom) {
        return (root, query, cb) -> dateFrom != null ? cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom) : null;
    }

    public static Specification<Document> createdAtBefore(LocalDateTime dateTo) {
        return (root, query, cb) -> dateTo != null ? cb.lessThanOrEqualTo(root.get("createdAt"), dateTo) : null;
    }

    public static Specification<Document> updatedAtAfter(LocalDateTime dateFrom) {
        return (root, query, cb) -> dateFrom != null ? cb.greaterThanOrEqualTo(root.get("updatedAt"), dateFrom) : null;
    }

    public static Specification<Document> updatedAtBefore(LocalDateTime dateTo) {
        return (root, query, cb) -> dateTo != null ? cb.lessThanOrEqualTo(root.get("updatedAt"), dateTo) : null;
    }

    public static Specification<Document> buildSearchCriteria(
        DocumentStatus status,
        String author,
        LocalDateTime dateFrom,
        LocalDateTime dateTo,
        boolean updated
    ) {
        if (updated) {
            return Specification.where(hasStatus(status))
                    .and(hasAuthor(author))
                    .and(updatedAtAfter(dateFrom))
                    .and(updatedAtBefore(dateTo));
        } else {
            return Specification.where(hasStatus(status))
                    .and(hasAuthor(author))
                    .and(createdAtAfter(dateFrom))
                    .and(createdAtBefore(dateTo));
        }
    }

}
