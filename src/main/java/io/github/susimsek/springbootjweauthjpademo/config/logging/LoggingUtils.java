package io.github.susimsek.springbootjweauthjpademo.config.logging;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Utility class for determining whether HTTP request/response bodies should be logged.
 */
@UtilityClass
public class LoggingUtils {

    /**
     * Determines if a request body should be logged based on HTTP method, content type, and size.
     *
     * @param content            the raw request body bytes
     * @param method             the HTTP method name (e.g., "GET", "POST")
     * @param contentTypeHeader  the Content-Type header value
     * @param maxBytes           maximum allowed payload size in bytes
     * @return true if the body should be logged, false otherwise
     */
    public boolean shouldLogRequestBody(byte[] content,
                                        String method,
                                        String contentTypeHeader,
                                        long maxBytes) {
        if (content == null || content.length == 0) {
            return false;
        }
        String methodUpper = method.toUpperCase(Locale.ROOT);
        if (HttpMethod.GET.matches(methodUpper)
            || HttpMethod.HEAD.matches(methodUpper)
            || HttpMethod.OPTIONS.matches(methodUpper)) {
            return false;
        }
        MediaType mediaType = MediaType.parseMediaType(
            StringUtils.hasText(contentTypeHeader) ? contentTypeHeader : MediaType.APPLICATION_JSON_VALUE
        );
        if (MediaType.MULTIPART_FORM_DATA.includes(mediaType)
            || MediaType.APPLICATION_OCTET_STREAM.includes(mediaType)) {
            return false;
        }
        if (content.length > maxBytes) {
            return false;
        }
        return true;
    }

    /**
     * Determines if a response body should be logged based on status code, content type, and size.
     *
     * @param statusCode         the HTTP status code
     * @param contentTypeHeader  the Content-Type header value
     * @param content            the raw response body bytes
     * @param maxBytes           maximum allowed payload size in bytes
     * @return true if the body should be logged, false otherwise
     */
    public boolean shouldLogResponseBody(int statusCode,
                                         String contentTypeHeader,
                                         byte[] content,
                                         long maxBytes) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null
            || status.is1xxInformational()
            || status.is3xxRedirection()
            || status == HttpStatus.NO_CONTENT
            || status == HttpStatus.NOT_MODIFIED) {
            return false;
        }
        MediaType mediaType = MediaType.parseMediaType(
            StringUtils.hasText(contentTypeHeader) ? contentTypeHeader : MediaType.APPLICATION_JSON_VALUE
        );
        if (MediaType.MULTIPART_FORM_DATA.includes(mediaType)
            || MediaType.APPLICATION_OCTET_STREAM.includes(mediaType)) {
            return false;
        }
        if (content == null || content.length == 0) {
            return false;
        }
        return content.length <= maxBytes;
    }
}
