package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CryptoUtil {
    private static final String ALGORITHM       = "AES/GCM/NoPadding";
    private static final int    IV_LENGTH_BYTES = 12;   // 96 bit
    private static final int    TAG_LENGTH_BITS = 128;  // 128 bit

    private final SecretKey key;

    public CryptoUtil(ApplicationProperties props) {
        byte[] keyBytes = Base64.getDecoder()
            .decode(props.getSecurity().getEncryption().getBase64Secret());
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (!StringUtils.hasText(plaintext)) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buf = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buf.put(iv).put(cipherBytes);
            return Base64.getEncoder().encodeToString(buf.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error encrypting data", e);
        }
    }


    public String decrypt(String cipherText) {
        if (!StringUtils.hasText(cipherText)) {
            return null;
        }
        try {
            byte[] ivAndCipher = Base64.getDecoder().decode(cipherText);
            ByteBuffer buf = ByteBuffer.wrap(ivAndCipher);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            buf.get(iv);
            byte[] cipherBytes = new byte[buf.remaining()];
            buf.get(cipherBytes);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Error decrypting data", e);
        }
    }
}
