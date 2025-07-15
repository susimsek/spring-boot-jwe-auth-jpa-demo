package io.github.susimsek.springbootjweauthjpademo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecaptchaDTO(
    boolean success,
    @JsonProperty("challenge_ts")
    String challengeTs,
    String hostname,
    @JsonProperty("error-codes")
    List<String> errorCodes,
    double score,
    String action
) {}
