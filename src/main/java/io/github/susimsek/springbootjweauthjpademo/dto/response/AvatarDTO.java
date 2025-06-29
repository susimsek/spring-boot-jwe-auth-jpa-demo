package io.github.susimsek.springbootjweauthjpademo.dto.response;

import java.time.Instant;

public record AvatarDTO(
    String contentType,
    byte[] content,
    Instant lastModified,
    String contentHash
) { }
