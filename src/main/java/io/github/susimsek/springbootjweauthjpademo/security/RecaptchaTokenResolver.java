package io.github.susimsek.springbootjweauthjpademo.security;

import jakarta.servlet.http.HttpServletRequest;

public interface RecaptchaTokenResolver {

    String resolve(HttpServletRequest request);
}
