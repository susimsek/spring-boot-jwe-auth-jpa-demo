package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Password;

@Schema(
    name = "ChangePasswordRequest",
    description = "Payload for changing a user's password"
)
public record ChangePasswordRequestDTO(

    @Schema(
        description = "The user's current password",
        example = "OldP@ssw0rd"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    String currentPassword,

    @Schema(
        description = "The new password",
        example = "NewP@ssw0rd1"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    @Password
    String newPassword

) {}
