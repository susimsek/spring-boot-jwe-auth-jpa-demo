package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Name;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Password;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Username;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
    name = "RegisterRequest",
    description = "Payload for new user registration"
)
public record RegisterRequestDTO(

    @Schema(
        description = "Unique username",
        example = "john_doe"
    )
    @NotBlank
    @Size(min = 3, max = 50)
    @Username
    String username,

    @Schema(
        description = "Password",
        example = "P@ssw0rd123"
    )
    @NotBlank
    @Size(min = 8, max = 64)
    @Password
    String password,

    @Schema(
        description = "Valid email address",
        example = "user@example.com"
    )
    @NotBlank
    @Email
    String email,

    @Schema(
        description = "First name",
        example = "Ayşe"
    )
    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    String firstName,

    @Schema(
        description = "Last name",
        example = "Yılmaz"
    )
    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    String lastName,


    @Schema(description = "Whether MFA (TOTP) is enabled for this user", example = "false")
    @NotNull
    Boolean mfaEnabled

) {}
