package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "VerifyTotpResult",
    description = "TOTP verification result"
)
public record VerifyTotpResultDTO(

    @Schema(
        description = "The authenticated user's profile information"
    )
    ProfileDTO profile,

    @Schema(
        description = "The newly issued JWT access token"
    )
    TokenDTO accessToken,

    @Schema(
        description = "The newly issued refresh token"
    )
    TokenDTO refreshToken

) {}
