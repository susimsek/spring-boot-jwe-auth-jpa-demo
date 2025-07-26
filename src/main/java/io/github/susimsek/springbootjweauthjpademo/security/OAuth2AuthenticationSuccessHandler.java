package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.mapper.AuthorityMapper;
import io.github.susimsek.springbootjweauthjpademo.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final AuthorityMapper authorityMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        List<String> roles = authorityMapper.toAuthorityNames(principal.getAuthorities());
        var accessToken = jwtUtil.generateAccessToken(principal.getId(), roles, false);
        var refreshToken = refreshTokenService.createRefreshToken(principal.getId());

        ResponseCookie accessCookie = CookieUtils.createAccessTokenCookie(accessToken);
        ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(refreshToken);
        res.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        res.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String redirectUrl = "/oauth2/callback?token=" + accessToken.token();
        res.sendRedirect(redirectUrl);
    }
}
