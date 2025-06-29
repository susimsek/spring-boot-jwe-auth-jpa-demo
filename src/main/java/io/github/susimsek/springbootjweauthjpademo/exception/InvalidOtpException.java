package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;

@Slf4j
public class InvalidOtpException extends BadCredentialsException {

    public InvalidOtpException(String msg) {
        super(msg);
        log.debug("InvalidOtpException thrown: {}", msg);
    }
}
