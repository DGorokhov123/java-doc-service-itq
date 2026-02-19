package ru.dgorokhov.docservice.dto;

import ru.dgorokhov.docservice.enums.ApproveResultStatus;

public record ApproveResult(
        Long id,
        boolean success,
        ApproveResultStatus status,
        String message
) {
}
