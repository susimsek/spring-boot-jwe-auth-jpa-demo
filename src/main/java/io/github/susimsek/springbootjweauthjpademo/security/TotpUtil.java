package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class TotpUtil {

    private final ApplicationProperties applicationProperties;

    public String generateSecret() {
        return Base32.random();
    }


    public boolean verifyCode(String secret, String code) {
        Totp totp = new Totp(secret);
        return totp.verify(code);
    }

    public String getUriForImage(String username, String secret) {
        String issuer = applicationProperties.getSecurity().getMfa().getIssuer();
        return "otpauth://totp/"
            + issuer + ":" + username
            + "?secret=" + secret
            + "&issuer=" + issuer;
    }

    public String generateQrUrl(String username, String secret) {
        String totpUri = getUriForImage(username, secret);

        String encoded = URLEncoder.encode(totpUri, StandardCharsets.UTF_8);
        String size = "200x200";
        String baseUrl = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .build()
            .toUriString();

        return baseUrl
            + "/api/v1/qrcode?data=" + encoded
            + "&size=" + size;
    }
}
