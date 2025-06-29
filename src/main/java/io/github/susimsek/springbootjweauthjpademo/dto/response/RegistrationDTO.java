package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "Registration",
    description = "Details returned after successful registration")
public record RegistrationDTO(

    @Schema(description = "Unique user identifier", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "Username chosen by the user", example = "john_doe")
    String username,

    @Schema(description = "User's email address", example = "user@example.com")
    String email,

    @Schema(description = "User's first name", example = "Ayşe")
    String firstName,

    @Schema(description = "User's last name", example = "Yılmaz")
    String lastName,

    @Schema(description = "Whether MFA (TOTP) is enabled for this user", example = "false")
    boolean mfaEnabled

) {}
