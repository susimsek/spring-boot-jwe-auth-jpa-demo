package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "MfaStatus",
    description = "User’s MFA enabled/verified flags"
)
public record MfaStatusDTO(

    @Schema(
        description = "Whether MFA is enabled for the user",
        example     = "true"
    )
    boolean mfaEnabled,

    @Schema(
        description = "Whether the user has completed MFA verification",
        example     = "false"
    )
    boolean mfaVerified

) {}
