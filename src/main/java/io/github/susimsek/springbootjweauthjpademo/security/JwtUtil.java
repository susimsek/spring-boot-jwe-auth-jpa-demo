package io.github.susimsek.springbootjweauthjpademo.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.jwk.RSAKey;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.github.susimsek.springbootjweauthjpademo.security.SecurityUtils.AUTHORITIES_KEY;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtEncoder jwtEncoder;
    private final RSAKey signingKey;
    private final RSAKey encryptionKey;
    private final ApplicationProperties props;
    private final JwtDecoder jwtDecoder;


    public TokenDTO generateMfaToken(String subject) {
        Instant now = Instant.now();
        long mfaExpiresIn = props.getSecurity().getJwt().getMfaExpirationDuration().getSeconds();
        Instant exp = now.plusSeconds(mfaExpiresIn);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.getSecurity().getJwt().getIssuer())
            .issuedAt(now)
            .expiresAt(exp)
            .subject(subject)
            .claim("token_use", "mfa")
            .claim("amr", List.of("pwd"))
            .build();

        String mfaToken = createJwtString(claims);
        return new TokenDTO(mfaToken, "Bearer", mfaExpiresIn);
    }

    public TokenDTO generateAccessToken(String subject,
                                        List<String> roles,
                                        boolean mfaEnabled) {

        Instant now = Instant.now();
        long accessExpiresIn = props.getSecurity().getJwt().getExpirationDuration().getSeconds();
        Instant exp = now.plusSeconds(accessExpiresIn);

        List<String> amr = new ArrayList<>();
        amr.add("pwd");
        if (mfaEnabled) {
            amr.add("mfa");
        }

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.getSecurity().getJwt().getIssuer())
            .issuedAt(now)
            .expiresAt(exp)
            .subject(subject)
            .claim(AUTHORITIES_KEY, roles)
            .claim("token_use", "access")
            .claim("amr", amr)
            .build();

        String accessToken = createJwtString(claims);
        return new TokenDTO(accessToken, "Bearer", accessExpiresIn);
    }

    public TokenDTO generateRefreshToken(String subject,
                                         String jti) {
        Instant now = Instant.now();
        long refreshExpiresIn = props.getSecurity().getJwt().getRefreshExpirationDuration().getSeconds();
        Instant exp = now.plusSeconds(refreshExpiresIn);

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(props.getSecurity().getJwt().getIssuer())
            .issuedAt(now)
            .expiresAt(exp)
            .subject(subject)
            .id(jti)
            .claim("token_use", "refresh")
            .build();

        String token = createJwtString(claims);
        return new TokenDTO(
            token, "Bearer", refreshExpiresIn);
    }



    private String createJwtString(JwtClaimsSet claims) {
        try {
            JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(signingKey.getKeyID())
                .build();

            String jws = jwtEncoder
                .encode(JwtEncoderParameters.from(jwsHeader, claims))
                .getTokenValue();

            if (!props.getSecurity().getJwt().isJweEnabled()) {
                return jws;
            }

            JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM)
                .contentType("JWT") // içteki jws’nin türü
                .keyID(encryptionKey.getKeyID())
                .build();

            JWEObject jweObject = new JWEObject(jweHeader, new Payload(jws));
            jweObject.encrypt(new RSAEncrypter(encryptionKey.toRSAPublicKey()));

            return jweObject.serialize();
        } catch (JOSEException ex) {
            throw new JwtEncodingException("Failed to create JWE", ex);
        }
    }

    public Jwt decodeToken(String refreshToken) {
        return jwtDecoder.decode(refreshToken);
    }
}
