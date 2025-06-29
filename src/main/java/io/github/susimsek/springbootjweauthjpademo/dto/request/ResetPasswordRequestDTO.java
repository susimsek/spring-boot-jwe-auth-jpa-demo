package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name        = "ResetPasswordRequest",
    description = "Token and new password for resetting a user’s password"
)
public record ResetPasswordRequestDTO(

    @Schema(
        description = "The password reset token sent via email",
        example     = "d290f1ee-6c54-4b01-90e6-d701748f0851"
    )
    @NotBlank
    String token,

    @Schema(
        description = "The new password for the user; must be 8–64 chars, include uppercase, lowercase, and a digit",
        example     = "NewPassw0rd!"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    @Password
    String newPassword

) {}
