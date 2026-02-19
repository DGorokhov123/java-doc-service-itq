package ru.dgorokhov.docservice.mapper;

import ru.dgorokhov.docservice.dto.DocumentResponseDto;
import ru.dgorokhov.docservice.dto.HistoryEntryResponseDto;
import ru.dgorokhov.docservice.dal.Document;

import java.util.List;

public class DocumentMapper {

    public static DocumentResponseDto toDto(Document document) {
        return DocumentResponseDto.builder()
                .id(document.getId())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    public static DocumentResponseDto toDto(Document document, List<HistoryEntryResponseDto> history) {
        return DocumentResponseDto.builder()
                .id(document.getId())
                .number(document.getNumber())
                .author(document.getAuthor())
                .title(document.getTitle())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .history(history)
                .build();
    }

}
