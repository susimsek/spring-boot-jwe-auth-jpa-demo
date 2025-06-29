package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateMessageRequest", description = "Payload for creating a new message entry")
public record CreateMessageRequestDTO(
    @NotBlank
    @Size(min = 2, max = 10)
    @Schema(description = "Locale code", example = "en")
    String locale,

    @NotBlank
    @Size(min = 3, max = 100)
    @Schema(description = "Message code", example = "jakarta.validation.constraints.NotBlank.message")
    String code,

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Localized message content", example = "Field cannot be blank.")
    String content
) {}
