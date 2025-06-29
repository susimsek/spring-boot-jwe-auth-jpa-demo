package io.github.susimsek.springbootjweauthjpademo.exception;

public class ImageProcessingException extends RuntimeException {
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
