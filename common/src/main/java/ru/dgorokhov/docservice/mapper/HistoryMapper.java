package ru.dgorokhov.docservice.mapper;

import ru.dgorokhov.docservice.dto.HistoryEntryResponseDto;
import ru.dgorokhov.docservice.dal.History;

public class HistoryMapper {

    public static HistoryEntryResponseDto toDto(History history) {
        return HistoryEntryResponseDto.builder()
                .id(history.getId())
                .action(history.getAction())
                .initiator(history.getInitiator())
                .comment(history.getComment())
                .createdAt(history.getCreatedAt())
                .build();
    }

}
