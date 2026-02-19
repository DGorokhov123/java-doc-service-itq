package ru.dgorokhov.docservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDocumentRequestDto {

    @NotEmpty(message = "Document IDs cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 document IDs allowed")
    private List<Long> documentIds;

    private String initiator;

    private String comment;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private Boolean ascending = false;
}
