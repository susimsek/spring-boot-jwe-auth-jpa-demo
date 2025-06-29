package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "Token",
    description = "JWT token details"
)
public record TokenDTO(

    @Schema(
        description = "The JWT string",
        example     = "eyJhbGciOiJIU..."
    )
    String token,

    @Schema(
        description = "The token type (e.g., Bearer)",
        example     = "Bearer"
    )
    String tokenType,

    @Schema(
        description = "Time in seconds until the token expires",
        example     = "3600"
    )
    Long tokenExpiresIn

) {}
