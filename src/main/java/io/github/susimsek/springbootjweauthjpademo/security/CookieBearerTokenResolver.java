package io.github.susimsek.springbootjweauthjpademo.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String ACCESS_TOKEN_PARAMETER_NAME = "access_token";
    private static final Pattern AUTHORIZATION_PATTERN =
        Pattern.compile("^Bearer (?<token>[A-Za-z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);

    private boolean allowFormEncodedBodyParameter = false;
    private boolean allowUriQueryParameter       = false;
    private boolean allowCookie                  = true;
    private String  bearerTokenHeaderName       = "Authorization";
    private String  cookieName                  = "accessToken";
    private String  mfaTokenCookieName          = "mfaToken";
    private String  refreshTokenCookieName      = "refreshToken";

    private final RequestMatcher refreshMatcher =
        PathPatternRequestMatcher
            .withDefaults()
            .matcher(HttpMethod.POST, "/api/auth/refresh-token");

    @Override
    public String resolve(HttpServletRequest request) {
        String headerToken = resolveFromAuthorizationHeader(request);
        String queryToken  = resolveAccessTokenFromQueryString(request);
        String bodyToken   = resolveAccessTokenFromBody(request);

        if (headerToken != null || queryToken != null || bodyToken != null) {
            return resolveToken(headerToken, queryToken, bodyToken, null);
        }

        if (refreshMatcher.matches(request)) {
           return resolveFromCookie(request, refreshTokenCookieName);
        }

        String accessCookie = resolveFromCookie(request, cookieName);

        String mfaCookie = resolveFromCookie(request, mfaTokenCookieName);

        if (accessCookie != null && mfaCookie != null) {
            BearerTokenError error = BearerTokenErrors.invalidRequest(
                "Found multiple bearer tokens in the request");
            throw new OAuth2AuthenticationException(error);
        }

        return (accessCookie != null) ? accessCookie : mfaCookie;
    }

    private String resolveFromAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader(this.bearerTokenHeaderName);
        if (!StringUtils.hasText(authorization) || !authorization.toLowerCase().startsWith("bearer")) {
            return null;
        }
        Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization.trim());
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }

    private String resolveAccessTokenFromQueryString(HttpServletRequest request) {
        if (allowUriQueryParameter && HttpMethod.GET.matches(request.getMethod())) {
            return resolveToken(request.getParameterValues(ACCESS_TOKEN_PARAMETER_NAME));
        }
        return null;
    }

    private String resolveAccessTokenFromBody(HttpServletRequest request) {
        if (allowFormEncodedBodyParameter
            && HttpMethod.POST.matches(request.getMethod())
            && "application/x-www-form-urlencoded".equals(request.getContentType())) {
            return resolveToken(request.getParameterValues(ACCESS_TOKEN_PARAMETER_NAME));
        }
        return null;
    }

    private String resolveFromCookie(HttpServletRequest request, String cookieName) {
        if (!allowCookie || request.getCookies() == null) {
            return null;
        }

        return CookieUtils.resolveToken(request, cookieName);
    }

    private String resolveToken(String... tokens) {
        String found = null;
        for (String token : tokens) {
            if (token == null) continue;
            if (found != null) {
                BearerTokenError error = BearerTokenErrors.invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }
            found = token;
        }
        if (found != null && found.isBlank()) {
            BearerTokenError error = BearerTokenErrors.invalidRequest("The requested token parameter is an empty string");
            throw new OAuth2AuthenticationException(error);
        }
        return found;
    }
}
