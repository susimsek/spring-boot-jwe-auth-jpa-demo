package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "EmailVerificationResult",
    description = "Result of email confirmation"
)
public record EmailVerificationDTO(

    @Schema(
        description = "Verification status",
        example     = "VERIFIED"
    )
    String status,

    @Schema(
        description = "Indicates if MFA is enabled for the user",
        example     = "true"
    )
    boolean mfaEnabled,

    @Schema(
        description = "JWT MFA token to continue authentication if MFA is enabled",
        nullable    = true
    )
    TokenDTO mfaToken

) {}
