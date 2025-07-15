package io.github.susimsek.springbootjweauthjpademo.config.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
public class Obfuscator {

    private final Set<String> sensitiveHeaders;
    private final Set<String> sensitiveCookies;
    private final Set<String> sensitiveParameters;
    private final Set<String> sensitivePaths;
    private final Set<String> sensitiveJsonBodyFields;
    private final String replacement;
    private final ObjectMapper objectMapper;

    public Obfuscator(ObjectMapper objectMapper, ApplicationProperties props) {
        this.replacement = props.getLogging().getObfuscate().getReplacement();
        this.sensitiveHeaders = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.sensitiveHeaders.addAll(props.getLogging().getObfuscate().getHeaders());
        this.sensitiveCookies = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        this.sensitiveCookies.addAll(props.getLogging().getObfuscate().getCookies());
        this.sensitiveParameters = props.getLogging().getObfuscate().getParameters();
        this.sensitiveJsonBodyFields = props.getLogging().getObfuscate().getJsonBodyFields();
        this.sensitivePaths = props.getLogging().getObfuscate().getPaths();
        this.objectMapper = objectMapper;
    }


    public HttpHeaders maskHeaders(HttpHeaders headers) {
        HttpHeaders masked = new HttpHeaders();
        headers.forEach((name, values) -> {
            if ("cookie".equalsIgnoreCase(name)) {
                String combined = String.join("; ", values);
                masked.put(name, List.of(maskCookies(combined)));
            }  else if ("set-cookie".equalsIgnoreCase(name)) {
                String combined = String.join("; ", values);
                masked.put(name, List.of(maskCookies(combined)));
            }
            else if (sensitiveHeaders.contains(name)) {
                masked.put(name, List.of(replacement));
            } else {
                masked.put(name, values);
            }
        });
        return masked;
    }


    public Map<String, String[]> maskParameters(Map<String, String[]> params) {
        Map<String, String[]> masked = new LinkedHashMap<>();
        params.forEach((key, values) -> {
            if (sensitiveParameters.contains(key)) {
                masked.put(key, new String[]{replacement});
            } else {
                masked.put(key, values);
            }
        });
        return masked;
    }

    public String maskBody(String jsonBody) {
        if (jsonBody == null || jsonBody.isBlank() || sensitiveJsonBodyFields.isEmpty()) {
            return jsonBody;
        }
        try {
            JsonNode root = objectMapper.readTree(jsonBody);
            for (String expr : sensitiveJsonBodyFields) {
                String[] parts;
                if (expr.startsWith("$..")) {
                    // recursive descent: ".." then field name
                    String field = expr.substring(3);
                    parts = new String[] { "..", field };
                } else if (expr.startsWith("$.")) {
                    parts = expr.substring(2).split("\\.");
                } else {
                    parts = expr.split("\\.");
                }
                maskPath(root, parts, 0);
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            log.debug("Failed to obfuscate JSON body", e);
            return jsonBody;
        }
    }

    public String maskFormBody(String formBody) {
        if (formBody == null || formBody.isBlank() || sensitiveParameters.isEmpty()) {
            return formBody;
        }
        return Arrays.stream(formBody.split("&"))
            .map(pair -> {
                String[] kv = pair.split("=", 2);
                String key = kv[0];
                if (sensitiveParameters.contains(key)) {
                    return key + "=" + replacement;
                }
                return pair;
            })
            .collect(Collectors.joining("&"));
    }

    public String maskCookies(String cookieHeader) {
        return Arrays.stream(cookieHeader.split(";"))
            .map(String::trim)
            .map(pair -> {
                String[] kv = pair.split("=", 2);
                String key = kv[0];
                if (sensitiveCookies.contains(key)) {
                    return key + "=" + replacement;
                }
                return pair;
            })
            .collect(Collectors.joining("; "));
    }

    public String maskPath(String path) {
        if (path == null || path.isBlank() || sensitivePaths.isEmpty()) {
            return path;
        }
        String result = path;
        for (String pattern : sensitivePaths) {
            String regex = "^" + pattern.replaceAll("\\{[^}]+\\}", "[^/]+") + "$";
            String replacementPath = pattern.replaceAll("\\{[^}]+\\}", replacement);
            result = result.replaceAll(regex, replacementPath);
        }
        return result;
    }


    private void maskPath(JsonNode node, String[] parts, int idx) {
        if (node == null || idx >= parts.length) {
            return;
        }
        String part = parts[idx];
        boolean isLast = idx == parts.length - 1;

        // recursive descent operator
        if ("..".equals(part)) {
            // apply next part at current node
            maskPath(node, parts, idx + 1);
            // traverse into all children with same '..'
            if (node.isObject()) {
                Iterator<String> fieldNames = node.fieldNames();
                while (fieldNames.hasNext()) {
                    String key = fieldNames.next();
                    JsonNode child = node.get(key);
                    maskPath(child, parts, idx);
                }
            } else if (node.isArray()) {
                for (JsonNode el : node) {
                    maskPath(el, parts, idx);
                }
            }
            return;
        }

        // array element e.g. orders[0] or orders[*]
        if (part.matches("(.+)\\[(\\*|\\d+)\\]")) {
            String field = part.substring(0, part.indexOf('['));
            String inside = part.substring(part.indexOf('[') + 1, part.length() - 1);
            JsonNode arr = node.path(field);
            if (arr.isArray()) {
                for (int i = 0; i < arr.size(); i++) {
                    if ("*".equals(inside) || Integer.toString(i).equals(inside)) {
                        JsonNode el = arr.get(i);
                        if (isLast && el.isValueNode()) {
                          ((ArrayNode) arr).set(i, TextNode.valueOf(replacement));
                        } else {
                            maskPath(el, parts, idx + 1);
                        }
                    }
                }
            }
            return;
        }

        // wildcard '*' for any field
        if ("*".equals(part) && node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode child = node.get(key);
                if (isLast && child.isValueNode()) {
                    ((ObjectNode) node).put(key, replacement);
                } else {
                    maskPath(child, parts, idx + 1);
                }
            }
            return;
        }

        // normal field name
        if (node.isObject()) {
            JsonNode child = node.get(part);
            if (child != null) {
                if (isLast && child.isValueNode()) {
                    ((ObjectNode) node).put(part, replacement);
                } else {
                    maskPath(child, parts, idx + 1);
                }
            }
        }
    }
}
