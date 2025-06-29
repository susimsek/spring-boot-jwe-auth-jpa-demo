package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AvatarUploadResult", description = "Avatar upload response")
public record AvatarUploadDTO(

    @Schema(
        description = "URL of the newly uploaded avatar image",
        example = "https://api.example.com/api/account/123e4567-e89b-12d3-a456-426614174000/avatar?v=1616161616"
    )
    String imageUrl

) {}
