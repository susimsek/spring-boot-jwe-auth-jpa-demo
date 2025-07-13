package io.github.susimsek.springbootjweauthjpademo.config.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class JsonLogFormatter implements LogFormatter {

    private final ObjectMapper objectMapper;

    @Override
    public String format(HttpLog httpLog) {
        ObjectNode logNode = objectMapper.createObjectNode();
        logNode.put("source", httpLog.getSource().toString().toLowerCase());
        logNode.put("type", httpLog.getType().toString().toLowerCase());
        logNode.put("correlation", httpLog.getCorrelation());
        logNode.put("method", httpLog.getMethod().name());
        logNode.put("uri", httpLog.getUri().toString());
        logNode.put("path", httpLog.getUri().getPath());

        if (httpLog.getUri().getHost() != null) {
            logNode.put("host", httpLog.getUri().getHost());
        }

        if (httpLog.getDurationMs() != null) {
            logNode.put("duration", httpLog.getDurationMs() + "ms");
        }
        if (httpLog.getStatusCode() != null) {
            logNode.put("statusCode", httpLog.getStatusCode());
        }
        if (httpLog.getHeaders() != null) {
            logNode.set("headers", parseHeaders(httpLog.getHeaders()));
        }
        if (StringUtils.hasText(httpLog.getBody())) {
            logNode.set("body", parseBody(httpLog.getBody()));
        }
        if (httpLog.getParameters() != null) {
            logNode.set("parameters", parseParameters(httpLog.getParameters()));
        }

        return logNode.toPrettyString();
    }

    private JsonNode parseHeaders(HttpHeaders headers) {
        return objectMapper.valueToTree(headers);
    }

    private JsonNode parseParameters(Map<String, String[]> parameters) {
        return objectMapper.valueToTree(parameters);
    }

    private JsonNode parseBody(String bodyString) {
        try {
            return objectMapper.readTree(bodyString);
        } catch (IOException e) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("body", bodyString);
            return node;
        }
    }
}
