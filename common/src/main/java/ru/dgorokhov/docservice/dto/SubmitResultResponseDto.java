package ru.dgorokhov.docservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResultResponseDto {

    private Long documentId;
    private boolean success;
    private String status;
    private String message;
}
