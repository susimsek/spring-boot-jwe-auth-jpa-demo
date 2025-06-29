package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "DeleteAccountRequest",
    description = "Payload for deleting the current user's account"
)
public record DeleteAccountRequestDTO(

    @Schema(
        description = "The user's current password",
        example = "OldP@ssw0rd"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    String currentPassword

) {}
