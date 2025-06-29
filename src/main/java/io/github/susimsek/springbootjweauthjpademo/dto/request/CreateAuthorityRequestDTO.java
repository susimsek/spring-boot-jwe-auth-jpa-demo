package io.github.susimsek.springbootjweauthjpademo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.AuthorityName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateAuthorityRequest", description = "Payload for creating a new authority")
public record CreateAuthorityRequestDTO(

    @NotBlank
    @Size(min = 3, max = 50)
    @AuthorityName
    @Schema(description = "Authority name", example = "ROLE_ADMIN")
    String name,

    @Size(max = 100)
    @Schema(description = "Authority description", example = "Administrator role")
    String description,

    @NotNull
    @JsonProperty("protected")
    @Schema(
        description = "Whether this authority is protected (cannot be deleted)",
        example = "false"
    )
    Boolean protectedFlag

) {}
