package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name        = "TotpSetup",
    description = "Data required to configure TOTP in an authenticator app"
)
public record TotpDTO(

    @Schema(
        description = "Base32-encoded TOTP secret key",
        example     = "JBSWY3DPEHPK3PXP"
    )
    String plainSecret,

    @Schema(
        description = "URL to a QR code image that encodes the TOTP secret",
        example     = "https://example.com/api/qrcode?data=otpauth://totp/Example:user@example.com%3Fsecret%3DJBSWY3DPEHPK3PXP"
    )
    String qrCodeUrl

) {}
