package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.github.susimsek.springbootjweauthjpademo.domain.enumeration.FlowType;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.EnumValue;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.TotpCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name        = "ConfirmTotpRequest",
    description = "Validate a 6-digit TOTP code and its flow"
)
public record ConfirmTotpRequestDTO(

    @Schema(
        description = "The 6-digit TOTP code generated by the authenticator app",
        example     = "123456"
    )
    @NotBlank
    @Size(min = 6, max = 6)
    @TotpCode
    String code,

    @Schema(
        implementation = FlowType.class)
    @NotBlank
    @EnumValue(enumClass = FlowType.class)
    String flow

) {}
