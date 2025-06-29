package io.github.susimsek.springbootjweauthjpademo.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class WebUtils {


    public String getClientIp(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
            .map(h -> h.split(",")[0].trim())
            .orElseGet(request::getRemoteAddr);
    }


    public HttpHeaders toHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        var headerNames = request.getHeaderNames();
        if (headerNames != null) {
            headerNames.asIterator().forEachRemaining(name ->
                headers.put(name, Collections.list(request.getHeaders(name)))
            );
        }
        return headers;
    }

    public HttpHeaders toHeaders(HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        Collection<String> headerNames = response.getHeaderNames();
        if (headerNames != null) {
            headerNames.forEach(name ->
                response.getHeaders(name).forEach(value ->
                    headers.add(name, value)
                )
            );
        }
        return headers;
    }


    public static Map<String, String[]> parseQueryParams(String query) {
        if (!StringUtils.hasText(query)) {
            return Collections.emptyMap();
        }
        MultiValueMap<String, String> params = UriComponentsBuilder.newInstance()
            .query(query)
            .build()
            .getQueryParams();
        Map<String, String[]> result = new HashMap<>();
        params.forEach((key, values) -> result.put(key, values.toArray(new String[0])));
        return result;
    }
}
