package ru.dgorokhov.docservice.dal;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.dgorokhov.docservice.enums.HistoryAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_history", indexes = {
    @Index(name = "idx_history_document", columnList = "document_id"),
    @Index(name = "idx_history_action", columnList = "action"),
    @Index(name = "idx_history_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class History {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private HistoryAction action;

    @Column(name = "initiator", nullable = false, length = 255)
    private String initiator;

    @Column(name = "comment", length = 1000)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
