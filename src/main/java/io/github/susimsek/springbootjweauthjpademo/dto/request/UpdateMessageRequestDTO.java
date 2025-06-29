package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateMessageRequest", description = "Payload for updating an existing message entry")
public record UpdateMessageRequestDTO(

    @NotBlank
    @Size(min = 3, max = 100)
    @Schema(description = "Message key/code", example = "jakarta.validation.constraints.NotBlank.message")
    String code,

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Localized message content", example = "Field cannot be blank.")
    String content
) {}
