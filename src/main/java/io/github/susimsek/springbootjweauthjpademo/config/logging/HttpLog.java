package io.github.susimsek.springbootjweauthjpademo.config.logging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpLog {

    private HttpLogType type;

    private HttpMethod method;

    private URI uri;

    private Integer statusCode;

    private HttpHeaders headers;

    private String body;

    private Map<String, String[]> parameters;

    private Source source;

    private Long durationMs;
}
