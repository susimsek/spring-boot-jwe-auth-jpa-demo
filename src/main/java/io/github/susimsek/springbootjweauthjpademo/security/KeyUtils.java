package io.github.susimsek.springbootjweauthjpademo.security;

import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.experimental.UtilityClass;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@UtilityClass
public class KeyUtils {

    public RSAKey buildRsaKey(String pubPem,
                                     String privPem,
                                     String kid,
                                     boolean forSign) throws Exception {

        // strip headers/footers and whitespace
        String pubContent = pubPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
        String privContent = privPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");

        // decode
        byte[] decodedPub = Base64.getDecoder().decode(pubContent);
        byte[] decodedPriv = Base64.getDecoder().decode(privContent);

        // generate key objects
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(decodedPub));
        RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(decodedPriv));

        // build the JWK
        RSAKey.Builder builder = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(kid);

        if (forSign) {
            builder
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE);
        } else {
            builder
                .algorithm(JWEAlgorithm.RSA_OAEP_256)
                .keyUse(KeyUse.ENCRYPTION);
        }

        return builder.build();
    }
}
