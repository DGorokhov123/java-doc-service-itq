package ru.dgorokhov.docservice.dal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_registry", indexes = {
    @Index(name = "idx_registry_document", columnList = "document_id"),
    @Index(name = "idx_registry_approved_at", columnList = "approved_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RegistryEntry {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false, unique = true)
    private Long documentId;

    @Column(name = "document_number", nullable = false, length = 64)
    private String documentNumber;

    @Column(name = "approved_by", nullable = false, length = 255)
    private String approvedBy;

    @CreationTimestamp
    @Column(name = "approved_at", nullable = false, updatable = false)
    private LocalDateTime approvedAt;

}
