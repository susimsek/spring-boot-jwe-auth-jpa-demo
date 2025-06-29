package io.github.susimsek.springbootjweauthjpademo.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "LoginResult",
    description = "Contains user profile and authentication tokens"
)
public record LoginResultDTO(

    @Schema(
        description = "The authenticated user's profile information"
    )
    ProfileDTO profile,

    @Schema(
        description = "Full access token (null if MFA is required)",
        nullable    = true,
        example     = "{ \"token\": \"eyJhbGciOi...\", \"expiresIn\": 3600 }"
    )
    TokenDTO accessToken,

    @Schema(
        description = "Refresh token (for continuing the session)",
        nullable    = true,
        example     = "{ \"token\": \"eyJhbGciOi...\", \"expiresIn\": 3600 }"
    )
    TokenDTO refreshToken,

    @Schema(
        description = "Temporary MFA token (only present when additional verification is required)",
        nullable    = true,
        example     = "{ \"token\": \"mfa-token-xyz\", \"expiresIn\": 300 }"
    )
    TokenDTO mfaToken,

    @Schema(
        description = "Indicates whether the user must complete MFA setup",
        nullable    = true,
        example     = "true"
    )
    Boolean mfaSetupRequired

) {}
