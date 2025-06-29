package io.github.susimsek.springbootjweauthjpademo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
    name = "Profile",
    description = "User profile information"
)
public record ProfileDTO(

    @Schema(
        description = "The user's unique identifier",
        example     = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
    )
    String id,

    @Schema(
        description = "The user's username",
        example = "johndoe"
    )
    String username,

    @Schema(
        description = "The user's email address",
        example = "johndoe@example.com"
    )
    String email,

    @Schema(
        description = "The user's first name",
        example = "John"
    )
    String firstName,

    @Schema(
        description = "The user's last name",
        example = "Doe"
    )
    String lastName,

    @Schema(
        description = "URL of the user's avatar image",
        example = "https://api.example.com/api/account/123e4567-e89b-12d3-a456-426614174000/avatar?v=1616161616"
    )
    String imageUrl,

    @JsonProperty("protected")
    @Schema(
        description = "Whether this user is protected (cannot be deleted)",
        example = "true"
    )
    Boolean protectedFlag,

    @Schema(
        description = "The user's authorities",
        example = "[\"ROLE_USER\",\"ROLE_ADMIN\"]"
    )
    List<String> authorities

) {}
