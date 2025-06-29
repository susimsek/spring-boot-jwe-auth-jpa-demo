package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(name = "PartialUpdateMessageRequest", description = "Fields for partial update of a message entry")
public record PartialUpdateMessageRequestDTO(

    @Size(min = 3, max = 100)
    @Schema(description = "Message key/code", example = "jakarta.validation.constraints.NotBlank.message")
    String code,

    @Size(max = 500)
    @Schema(description = "Localized message content", example = "Field cannot be blank.")
    String content
) {}
