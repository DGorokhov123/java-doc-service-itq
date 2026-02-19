package ru.dgorokhov.docservice.mapper;

import ru.dgorokhov.docservice.dto.*;

import java.util.List;

public class BatchMapper {

    public static BatchSubmitResponseDto toSubmitDto(List<SubmitResult> results) {
        List<SubmitResultResponseDto> resultResponses = results.stream()
                .map(r -> SubmitResultResponseDto.builder()
                        .documentId(r.id())
                        .success(r.success())
                        .status(r.status().name())
                        .message(r.message())
                        .build())
                .toList();

        long successCount = results.stream().filter(SubmitResult::success).count();

        return BatchSubmitResponseDto.builder()
                .results(resultResponses)
                .successCount((int) successCount)
                .failureCount(results.size() - (int) successCount)
                .build();
    }

    public static BatchApproveResponseDto toApproveDto(List<ApproveResult> results) {
        List<ApproveResultResponseDto> resultResponses = results.stream()
                .map(r -> ApproveResultResponseDto.builder()
                        .documentId(r.id())
                        .success(r.success())
                        .status(r.status().name())
                        .message(r.message())
                        .build())
                .toList();

        long successCount = results.stream().filter(ApproveResult::success).count();

        return BatchApproveResponseDto.builder()
                .results(resultResponses)
                .successCount((int) successCount)
                .failureCount(results.size() - (int) successCount)
                .build();
    }

}
