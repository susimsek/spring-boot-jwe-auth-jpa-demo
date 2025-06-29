package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="RefreshToken", description="Contains the new refresh token and its subject")
public record RefreshTokenDTO(

    @Schema(description="The user ID (subject) from the token")
    String subject,

    @Schema(description="The newly issued refresh token details")
    TokenDTO refreshToken

) {}
