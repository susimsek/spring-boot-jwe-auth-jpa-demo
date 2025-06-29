package io.github.susimsek.springbootjweauthjpademo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Username;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Name;

import java.util.Set;

@Schema(name = "PartialUpdateUserRequest", description = "Fields for partial user update")
public record PartialUpdateUserRequestDTO(
    @Size(min = 3, max = 50)
    @Username
    @Schema(description = "Username", example = "johndoe")
    String username,

    @Email
    @Schema(description = "Email address", example = "johndoe@example.com")
    String email,

    @Size(min = 2, max = 50)
    @Name
    @Schema(description = "First name", example = "John")
    String firstName,

    @Size(min = 2, max = 50)
    @Name
    @Schema(description = "Last name", example = "Doe")
    String lastName,

    @Schema(description = "Whether the user is enabled or disabled", example = "true")
    Boolean enabled,

    @Schema(description = "Whether multi-factor authentication is enabled", example = "false")
    Boolean mfaEnabled,

    @Schema(description = "Whether the user account is locked", example = "false")
    Boolean locked,

    @JsonProperty("protected")
    @Schema(
        description = "Whether this user is protected (cannot be deleted)",
        example = "false"
    )
    Boolean protectedFlag,

    @Schema(description = "List of authorities to assign (if updating)", example = "[\"ROLE_USER\"]")
    Set<String> authorities
) {}
