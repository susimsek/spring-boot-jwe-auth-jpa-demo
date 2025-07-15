package io.github.susimsek.springbootjweauthjpademo.config.logging;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationDefaults;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.util.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private final LogFormatter formatter;
    private final Obfuscator obfuscator;
    private final ApplicationProperties.Logging props;

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    @NonNull
    public ClientHttpResponse intercept(
        @NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution)
        throws IOException {

        if (shouldNotIntercept(request)) {
            return execution.execute(request, body);
        }

        if (!props.getLevel().atLeast(HttpLogLevel.BASIC)) {
            return execution.execute(request, body);
        }

        // --- REQUEST LOG ---
        URI maskedUri = URI.create(obfuscator.maskPath(request.getURI().toString()));
        Map<String, String[]> maskedParams = obfuscator.maskParameters(
            WebUtils.parseQueryParams(request.getURI().getQuery()));
        HttpLog reqLog = createRequestLog(request, body, maskedUri, maskedParams);
        log.info(formatter.format(reqLog));

        // --- EXECUTE & TIME IT ---
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ClientHttpResponse response = execution.execute(request, body);
        stopWatch.stop();
        long durationMs = stopWatch.getTotalTimeMillis();

        ClientHttpResponse wrapped = new BufferingClientHttpResponseWrapper(response);

        // --- RESPONSE LOG ---
        HttpLog resLog = createResponseLog(request, wrapped, maskedParams, maskedUri, durationMs);
        log.info(formatter.format(resLog));

        return wrapped;
    }

    private String toPayload(byte[] buf) {
        if (buf == null || buf.length == 0) return null;
        String payload = new String(buf, StandardCharsets.UTF_8);
        return StringUtils.hasText(payload) ? payload : null;
    }

    private HttpLog createRequestLog(HttpRequest request, byte[] body,
                                     URI maskedUri,
                                     Map<String, String[]> maskedParams) {
        String contentTypeHeader = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean shouldLogBody = LoggingUtils.shouldLogRequestBody(
            body,
            request.getMethod().name(),
            contentTypeHeader,
            props.getMaxPayloadSize().toBytes()
        ) && props.getLevel().atLeast(HttpLogLevel.FULL);
        String requestBody = processBody(body, contentTypeHeader, shouldLogBody);
        // 3. Build log object
        return HttpLog.builder()
                .type(HttpLogType.REQUEST)
                .correlation(MDC.get("traceId"))
                .method(request.getMethod())
                .uri(maskedUri)
                .statusCode(null)
                .headers(props.getLevel().atLeast(HttpLogLevel.HEADERS)
                        ? obfuscator.maskHeaders(request.getHeaders())
                        : null)
                .body(requestBody)
                .parameters(maskedParams)
                .source(Source.CLIENT)
                .durationMs(null)
                .build();
    }

    private HttpLog createResponseLog(HttpRequest request,
                                      ClientHttpResponse response,
                                      Map<String, String[]> maskedParams,
                                      URI maskedUri,
                                      long durationMs) throws IOException {

        byte[] content = StreamUtils.copyToByteArray(response.getBody());
        String contentTypeHeader = response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        boolean shouldLogBody = LoggingUtils.shouldLogResponseBody(
            response.getStatusCode().value(),
            contentTypeHeader,
            content,
            props.getMaxPayloadSize().toBytes()
        ) && props.getLevel().atLeast(HttpLogLevel.FULL);
        String responseBody = processBody(content, contentTypeHeader, shouldLogBody);

        // 3. Build log object
        return HttpLog.builder()
                .type(HttpLogType.RESPONSE)
                .correlation(MDC.get("traceId"))
                .method(request.getMethod())
                .uri(maskedUri)
                .statusCode(response.getStatusCode().value())
                .headers(props.getLevel().atLeast(HttpLogLevel.HEADERS)
                        ? obfuscator.maskHeaders(response.getHeaders())
                        : null)
                .body(responseBody)
                .parameters(maskedParams)
                .source(Source.CLIENT)
                .durationMs(durationMs)
                .build();
    }

    private String processBody(byte[] content, String contentTypeHeader, boolean shouldLog) {
        if (!shouldLog) {
            return null;
        }
        String payload = toPayload(content);
        if (payload == null) {
            return null;
        }
        // Parse content type once
        MediaType mt = MediaType.parseMediaType(
            contentTypeHeader != null ? contentTypeHeader : MediaType.APPLICATION_JSON_VALUE
        );
        // Form-urlencoded bodies
        if (MediaType.APPLICATION_FORM_URLENCODED.includes(mt)) {
            return obfuscator.maskFormBody(payload);
        }
        // JSON bodies
        if (MediaType.APPLICATION_JSON.includes(mt) || mt.getSubtype().endsWith("+json")) {
            return obfuscator.maskBody(payload);
        }
        // Other content types - return as-is
        return payload;
    }

    private boolean shouldNotIntercept(HttpRequest request) {
        String path = request.getURI().getPath();
        return ApplicationDefaults.Logging.clientExcludeUriPatterns.stream()
            .anyMatch(pattern -> matcher.match(pattern, path));
    }

}
