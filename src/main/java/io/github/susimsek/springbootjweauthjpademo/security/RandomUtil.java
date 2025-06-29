package io.github.susimsek.springbootjweauthjpademo.security;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.RandomStringUtils;
import java.security.SecureRandom;

@UtilityClass
public class RandomUtil {
    private static final int DEF_COUNT = 32;
    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        SECURE_RANDOM.nextBytes(new byte[64]);
    }


    public String generateRandomAlphanumeric() {
        return RandomStringUtils.random(
            DEF_COUNT,
            0, 0,
            true,  // include letters
            true,  // include numbers
            null,  // no extra chars
            SECURE_RANDOM
        );
    }
}
