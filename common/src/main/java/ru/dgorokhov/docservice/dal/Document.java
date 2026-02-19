package ru.dgorokhov.docservice.dal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.dgorokhov.docservice.enums.DocumentStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents", indexes = {
    @Index(name = "idx_documents_status", columnList = "status"),
    @Index(name = "idx_documents_author", columnList = "author"),
    @Index(name = "idx_documents_created_at", columnList = "created_at"),
    @Index(name = "idx_documents_updated_at", columnList = "updated_at"),
    @Index(name = "idx_documents_number", columnList = "number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "number", nullable = false, unique = true, length = 64)
    private String number;

    @Column(name = "author", nullable = false, length = 255)
    private String author;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

}
