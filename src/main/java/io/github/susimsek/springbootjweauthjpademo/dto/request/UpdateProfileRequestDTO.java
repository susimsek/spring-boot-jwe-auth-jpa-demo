package io.github.susimsek.springbootjweauthjpademo.dto.request;

import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Name;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    name = "UpdateProfileRequest",
    description = "Payload to update user’s profile"
)
public record UpdateProfileRequestDTO(

    @Schema(
        description = "User's first name",
        example = "Ayşe"
    )
    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    String firstName,

    @Schema(
        description = "User's last name",
        example = "Yılmaz"
    )
    @NotBlank
    @Size(min = 2, max = 50)
    @Name
    String lastName

) {}
