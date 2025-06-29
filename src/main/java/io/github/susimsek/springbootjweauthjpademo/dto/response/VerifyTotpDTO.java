package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "VerifyTotpResult",
    description = "TOTP verification outcome with optional token and profile"
)
public record VerifyTotpDTO(

    @Schema(
        description = "Whether MFA has been successfully verified",
        example     = "true"
    )
    boolean mfaVerified,

    @Schema(
        description = "JWT access token granted after successful TOTP verification (only for login flow)",
        nullable    = true
    )
    TokenDTO accessToken,

    @Schema(
        description = "Refresh token value and its metadata for continued sessions",
        nullable    = true
    )
    TokenDTO refreshToken,

    @Schema(
        description = "User profile information returned after successful TOTP verification (only for login flow)",
        nullable    = true
    )
    ProfileDTO profile

) {}
