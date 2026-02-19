package ru.dgorokhov.docservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequestDto {

    @NotBlank(message = "Author cannot be empty")
    @Size(max = 255, message = "Author must be less than 255 characters")
    private String author;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 500, message = "Title must be less than 500 characters")
    private String title;

}
