package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.dto.response.TokenDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

@UtilityClass
public class CookieUtils {

    public static final String ACCESS_COOKIE_NAME = "accessToken";
    public static final String MFA_COOKIE_NAME    = "mfaToken";
    public static final String REFRESH_COOKIE_NAME = "refreshToken";

    public ResponseCookie createAccessTokenCookie(TokenDTO tokenDto) {
        return ResponseCookie.from(ACCESS_COOKIE_NAME, tokenDto.token())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(tokenDto.tokenExpiresIn())
            .sameSite("Strict")
            .build();
    }

    public ResponseCookie removeAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();
    }

    public ResponseCookie createMfaTokenCookie(TokenDTO tokenDto) {
        return ResponseCookie.from(MFA_COOKIE_NAME, tokenDto.token())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(tokenDto.tokenExpiresIn())
            .sameSite("Strict")
            .build();
    }

    public ResponseCookie removeMfaTokenCookie() {
        return ResponseCookie.from(MFA_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();
    }

    public ResponseCookie createRefreshTokenCookie(TokenDTO tokenDto) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, tokenDto.token())
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(tokenDto.tokenExpiresIn())
            .sameSite("Strict")
            .build();
    }

    /** Removes the refresh token cookie. */
    public ResponseCookie removeRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0)
            .sameSite("Strict")
            .build();
    }

    public String resolveToken(HttpServletRequest request, String cookieName) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie == null) {
            return null;
        }
        String val = cookie.getValue();
        return StringUtils.hasText(val) ? val : null;
    }
}
