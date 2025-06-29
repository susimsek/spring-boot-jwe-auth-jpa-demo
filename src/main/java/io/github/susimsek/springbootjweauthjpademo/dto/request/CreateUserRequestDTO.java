package io.github.susimsek.springbootjweauthjpademo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Name;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Password;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Username;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(name = "CreateUserRequest", description = "Payload for creating a new user")
public record CreateUserRequestDTO(
    @NotBlank
    @Size(min = 3, max = 50)
    @Username
    @Schema(description = "New user's username", example = "johndoe")
    String username,

    @NotBlank
    @Size(min = 8, max = 64)
    @Password
    @Schema(description = "New user's password", example = "P@ssw0rd!")
    String password,

    @NotBlank
    @Email
    @Schema(description = "New user's email address", example = "johndoe@example.com")
    String email,

    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    @Schema(description = "New user's first name", example = "John")
    String firstName,

    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    String lastName,

    @NotEmpty
    @Schema(description = "List of authorities to assign", example = "[\"ROLE_USER\"]")
    Set<String> authorities,

    @NotNull
    @Schema(description = "Whether the user is enabled or disabled", example = "true")
    Boolean enabled,

    @NotNull
    @Schema(description = "Whether multi-factor authentication is enabled", example = "false")
    Boolean mfaEnabled,

    @NotNull
    @JsonProperty("protected")
    @Schema(
        description = "Whether this user is protected (cannot be deleted)",
        example = "false"
    )
    Boolean protectedFlag
) {}
