package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ChangePasswordResult",
    description = "Password change result"
)
public record ChangePasswordDTO(

    @Schema(
        description = "Indicates the outcome of the password change operation",
        example = "SUCCESS"
    )
    String status

) {}

