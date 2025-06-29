package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name        = "LoginRequest",
    description = "Credentials for user login"
)
public record LoginRequestDTO(

    @Schema(
        description = "The username of the user",
        example     = "john_doe"
    )
    @NotBlank
    @Size(min = 3, max = 50)
    String username,

    @Schema(
        description = "The user's password",
        example     = "P@ssw0rd123"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    String password

) {}
