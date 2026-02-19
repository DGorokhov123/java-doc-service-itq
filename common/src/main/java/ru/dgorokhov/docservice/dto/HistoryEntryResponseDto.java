package ru.dgorokhov.docservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.dgorokhov.docservice.enums.HistoryAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryEntryResponseDto {

    private Long id;
    private HistoryAction action;
    private String initiator;
    private String comment;
    private LocalDateTime createdAt;

}
