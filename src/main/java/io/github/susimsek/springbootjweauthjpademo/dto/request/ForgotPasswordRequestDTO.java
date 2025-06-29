package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(
    name        = "ForgotPasswordRequest",
    description = "Indicates that the password reset email has been queued"
)
public record ForgotPasswordRequestDTO(

    @Schema(
        description = "The email address of the user requesting password reset",
        example     = "user@example.com"
    )
    @NotBlank
    @Email
    String email

) {}
