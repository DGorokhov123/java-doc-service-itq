package ru.dgorokhov.docservice.dto;

import ru.dgorokhov.docservice.enums.SubmitResultStatus;

public record SubmitResult(
        Long id,
        boolean success,
        SubmitResultStatus status,
        String message
) {
}
