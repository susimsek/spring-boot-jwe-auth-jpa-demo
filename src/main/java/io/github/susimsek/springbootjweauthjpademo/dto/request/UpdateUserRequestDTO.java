package io.github.susimsek.springbootjweauthjpademo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Username;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Name;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(name = "UpdateUserRequest", description = "Payload for updating an existing user")
public record UpdateUserRequestDTO(

    @NotBlank
    @Size(min = 3, max = 50)
    @Username
    @Schema(description = "Username (if updating)", example = "johndoe")
    String username,

    @NotBlank
    @Email
    @Schema(description = "Email address (if updating)", example = "johndoe@example.com")
    String email,

    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    @Schema(description = "New user's first name", example = "John")
    String firstName,

    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    @Schema(description = "Last name", example = "Doe")
    String lastName,

    @NotNull
    @Schema(description = "Whether the user is enabled or disabled", example = "true")
    Boolean enabled,

    @NotNull
    @Schema(description = "Whether multi-factor authentication is enabled", example = "false")
    Boolean mfaEnabled,

    @NotNull
    @Schema(description = "Whether the user account is locked", example = "false")
    Boolean locked,

    @NotNull
    @JsonProperty("protected")
    @Schema(
        description = "Whether this user is protected (cannot be deleted)",
        example = "false"
    )
    Boolean protectedFlag,

    @NotEmpty
    @Schema(description = "List of authorities to assign", example = "[\"ROLE_USER\"]")
    Set<String> authorities
) {}
