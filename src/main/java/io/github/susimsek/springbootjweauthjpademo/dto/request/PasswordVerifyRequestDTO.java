package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name        = "PasswordVerifyRequest",
    description = "User’s current password payload"
)
public record PasswordVerifyRequestDTO(

    @Schema(
        description = "The user's current password",
        example     = "CorrectHorseBatteryStaple"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    String password

) {}
