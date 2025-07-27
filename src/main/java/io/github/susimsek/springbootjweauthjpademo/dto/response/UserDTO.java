package io.github.susimsek.springbootjweauthjpademo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;
import java.util.List;

@Schema(name = "User", description = "Basic user information")
@Relation(collectionRelation="users", itemRelation="user")
public record UserDTO(
    @Schema(description = "User ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "Username", example = "johndoe")
    String username,

    @Schema(description = "Email address", example = "johndoe@example.com")
    String email,

    @Schema(description = "First name", example = "John")
    String firstName,

    @Schema(description = "Last name", example = "Doe")
    String lastName,

    @Schema(description = "Avatar image URL", example = "https://.../avatar.png")
    String imageUrl,

    @Schema(description = "Assigned authorities", example = "[\"ROLE_USER\"]")
    List<String> authorities,

    @Schema(description = "Whether the user is enabled", example = "true")
    Boolean enabled,

    @JsonProperty("protected")
    @Schema(
        description = "Whether this authority is protected (cannot be deleted)",
        example = "true"
    )
    Boolean protectedFlag,

    @Schema(description = "Whether multi-factor authentication is enabled", example = "false")
    Boolean mfaEnabled,

    @Schema(description = "Whether the user account is locked", example = "false")
    Boolean locked,

    @Schema(description = "Authentication provider", example = "google")
    String provider,

    @Schema(description = "Timestamp when the user was created", example = "2025-06-13T15:47:38.786Z")
    Instant createdAt,

    @Schema(description = "Timestamp when the user was last updated", example = "2025-06-13T16:00:00.000Z")
    Instant updatedAt
) {}
