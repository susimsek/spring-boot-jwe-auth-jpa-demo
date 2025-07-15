package io.github.susimsek.springbootjweauthjpademo.config.logging;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationDefaults;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.util.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

    private final LogFormatter formatter;
    private final Obfuscator obfuscator;
    private final ApplicationProperties.Logging props;

    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return ApplicationDefaults.Logging.serverExcludeUriPatterns.stream()
            .anyMatch(pattern -> matcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
                                    throws ServletException, IOException {
        // Ensure correlation uses the current traceId
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            MDC.put("correlation", traceId);
        }
        if (!props.getLevel().atLeast(HttpLogLevel.BASIC)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper reqWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper resWrapper = new ContentCachingResponseWrapper(response);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        chain.doFilter(reqWrapper, resWrapper);
        stopWatch.stop();
        long durationMs = stopWatch.getTotalTimeMillis();

        Map<String, String[]> maskedParams = obfuscator.maskParameters(reqWrapper.getParameterMap());
        URI maskedUri = URI.create(obfuscator.maskPath(reqWrapper.getRequestURI()));

        HttpLog reqLog = createRequestLog(reqWrapper, maskedParams, maskedUri);
        log.info(formatter.format(reqLog));

        HttpLog resLog = createResponseLog(reqWrapper, resWrapper, maskedParams, maskedUri, durationMs);
        log.info(formatter.format(resLog));

        resWrapper.copyBodyToResponse();
    }

    private String toPayload(byte[] buf) {
        if (buf == null || buf.length == 0) return null;
        String payload = new String(buf, StandardCharsets.UTF_8);
        return StringUtils.hasText(payload) ? payload : null;
    }

    private HttpLog createRequestLog(ContentCachingRequestWrapper reqWrapper,
                                     Map<String, String[]> maskedParams,
                                     URI maskedUri) {
        String contentTypeHeader = reqWrapper.getContentType();
        boolean shouldLogBody = LoggingUtils.shouldLogRequestBody(
            reqWrapper.getContentAsByteArray(),
            reqWrapper.getMethod(),
            contentTypeHeader,
            props.getMaxPayloadSize().toBytes()
        ) && props.getLevel().atLeast(HttpLogLevel.FULL);
        String requestBody = processBody(
            reqWrapper.getContentAsByteArray(),
            reqWrapper.getContentType(),
            shouldLogBody
        );

        return HttpLog.builder()
            .type(HttpLogType.REQUEST)
            .correlation(MDC.get("traceId"))
            .method(HttpMethod.valueOf(reqWrapper.getMethod()))
            .uri(maskedUri)
            .statusCode(null)
            .headers(props.getLevel().atLeast(HttpLogLevel.HEADERS) ? obfuscator.maskHeaders(WebUtils.toHeaders(reqWrapper)) : null)
            .body(requestBody)
            .parameters(maskedParams)
            .source(Source.SERVER)
            .durationMs(null)
            .build();
    }

    private HttpLog createResponseLog(ContentCachingRequestWrapper reqWrapper,
                                      ContentCachingResponseWrapper resWrapper,
                                      Map<String, String[]> maskedParams,
                                      URI maskedUri,
                                      long durationMs) {
        String contentTypeHeader = resWrapper.getContentType();
        boolean shouldLogBody = LoggingUtils.shouldLogResponseBody(
            resWrapper.getStatus(),
            contentTypeHeader,
            resWrapper.getContentAsByteArray(),
            props.getMaxPayloadSize().toBytes()
        ) && props.getLevel().atLeast(HttpLogLevel.FULL);
        String responseBody = processBody(
            resWrapper.getContentAsByteArray(),
            resWrapper.getContentType(),
            shouldLogBody
        );

        return HttpLog.builder()
            .type(HttpLogType.RESPONSE)
            .correlation(MDC.get("traceId"))
            .method(HttpMethod.valueOf(reqWrapper.getMethod()))
            .uri(maskedUri)
            .statusCode(resWrapper.getStatus())
            .headers(props.getLevel().atLeast(HttpLogLevel.HEADERS) ? obfuscator.maskHeaders(WebUtils.toHeaders(resWrapper)) : null)
            .body(responseBody)
            .parameters(maskedParams)
            .source(Source.SERVER)
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

        MediaType mt = MediaType.parseMediaType(
            contentTypeHeader != null ? contentTypeHeader : MediaType.APPLICATION_JSON_VALUE
        );
        // Form-url-encoded bodies
        if (MediaType.APPLICATION_FORM_URLENCODED.includes(mt)) {
            return obfuscator.maskFormBody(payload);
        }
        // JSON bodies
        if (MediaType.APPLICATION_JSON.includes(mt) || mt.getSubtype().endsWith("+json")) {
            return obfuscator.maskBody(payload);
        }
        return payload;
    }
}
