package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "ChangeEmailRequest",
    description = "Payload to change email"
)
public record ChangeEmailRequestDTO(

    @Schema(
        description = "The user's current password for verification",
        example = "P@ssw0rd!"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    String currentPassword,

    @Schema(
        description = "The new email address to set",
        example = "newemail@example.com"
    )
    @NotBlank
    @Email
    String newEmail

) {}
