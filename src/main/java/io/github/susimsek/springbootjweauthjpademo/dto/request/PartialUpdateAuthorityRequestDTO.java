package io.github.susimsek.springbootjweauthjpademo.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.AuthorityName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(
    name = "PartialUpdateAuthorityRequest",
    description = "Fields for partial authority update"
)
public record PartialUpdateAuthorityRequestDTO(

    @Size(min = 3, max = 50)
    @AuthorityName
    @Schema(description = "Authority name", example = "ROLE_ADMIN")
    String name,

    @Size(max = 100)
    @Schema(description = "Authority description", example = "Administrator role")
    String description,

    @JsonProperty("protected")
    @Schema(
        description = "Whether this authority is protected (cannot be deleted)",
        example = "true"
    )
    Boolean protectedFlag

) {}
