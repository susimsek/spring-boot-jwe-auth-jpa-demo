package io.github.susimsek.springbootjweauthjpademo.config.resolver;

import io.github.susimsek.springbootjweauthjpademo.exception.ValidationException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.regex.Pattern;

@Schema(
    type    = "string",
    pattern = "^[1-9]\\d*x[1-9]\\d*$",
    description = "WIDTHxHEIGHT, both positive integers",
    example     = "200x200"
)
public record QrCodeSize(int width, int height) {

    private static final Pattern SIZE_PATTERN = Pattern.compile("^[1-9]\\d*x[1-9]\\d*$");

    public static QrCodeSize parse(String text) {
        if (text == null || !SIZE_PATTERN.matcher(text).matches()) {
            throw new ValidationException(
                ProblemType.QRCODE_SIZE_INVALID_FORMAT,
                text
            );
        }
        String[] parts = text.split("x");
        int w = Integer.parseInt(parts[0]);
        int h = Integer.parseInt(parts[1]);
        return new QrCodeSize(w, h);
    }
}
