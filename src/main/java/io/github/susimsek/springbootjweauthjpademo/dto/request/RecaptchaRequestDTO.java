package io.github.susimsek.springbootjweauthjpademo.dto.request;

public record RecaptchaRequestDTO(
    String secret,
    String response,
    String remoteip
) {}
