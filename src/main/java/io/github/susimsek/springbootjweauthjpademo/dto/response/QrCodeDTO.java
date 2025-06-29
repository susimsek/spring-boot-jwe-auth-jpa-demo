package io.github.susimsek.springbootjweauthjpademo.dto.response;

public record QrCodeDTO(
    byte[] content,
    String contentHash
) {}
