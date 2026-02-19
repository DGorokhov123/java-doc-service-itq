package ru.dgorokhov.docservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentApproveResponseDto {

    private Long documentId;
    private String documentNumber;
    private String finalStatus;
    private int totalAttempts;
    private int successfulApprovals;
    private int conflicts;
    private int notFoundErrors;
    private int registerErrors;
    private int otherErrors;

}
