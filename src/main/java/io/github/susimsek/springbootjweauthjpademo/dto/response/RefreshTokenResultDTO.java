package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "RefreshTokenResult",
    description = "Response containing both new access and refresh tokens"
)
public record RefreshTokenResultDTO(

    @Schema(
        description = "Newly issued access token and its expiry details",
        example     = "{ \"token\": \"eyJhbGciOi...\", \"expiresIn\": 3600 }"
    )
    TokenDTO accessToken,

    @Schema(
        description = "Newly issued refresh token and its expiry details",
        example     = "{ \"token\": \"eyJhbGciOi...\", \"expiresIn\": 3600 }"
    )
    TokenDTO refreshToken

) {}
