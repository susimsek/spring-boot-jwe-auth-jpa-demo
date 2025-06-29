package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ChangeEmailResult",
    description = "Email change result"
)
public record ChangeEmailDTO(

    @Schema(
        description = "Indicates the outcome of the email change operation",
        example = "VERIFICATION_SENT"
    )
    String status,

    @Schema(
        description = "The new email address (pending verification) or current email after confirmation",
        example = "user@example.com"
    )
    String email

) {}
