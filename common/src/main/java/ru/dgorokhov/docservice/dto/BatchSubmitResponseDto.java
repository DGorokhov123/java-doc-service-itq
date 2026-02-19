package ru.dgorokhov.docservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchSubmitResponseDto {

    private List<SubmitResultResponseDto> results;
    private int successCount;
    private int failureCount;

}
