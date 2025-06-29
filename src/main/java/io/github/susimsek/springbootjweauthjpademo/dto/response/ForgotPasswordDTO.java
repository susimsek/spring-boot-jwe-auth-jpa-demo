package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "ForgotPasswordResult",
    description = "Password reset request queued"
)
public record ForgotPasswordDTO(

    @Schema(
        description = "The status of the request",
        example     = "QUEUED"
    )
    String status

) {}
