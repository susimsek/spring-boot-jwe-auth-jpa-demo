package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;

@Getter
public class QRCodeGenerationException extends RuntimeException {
    public QRCodeGenerationException(String message) {
        super(message);
    }

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
