package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "ResetPasswordResult",
    description = "Result of a password reset operation"
)
public record ResetPasswordDTO(

    @Schema(
        description = "Status of the reset operation",
        example     = "SUCCESS"
    )
    String status

) {}
