package io.github.susimsek.springbootjweauthjpademo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Schema(name = "Authority", description = "Represents a single authority")
@Relation(collectionRelation="authorities", itemRelation="authority")
public record AuthorityDTO(
    @Schema(description = "Authority ID", example = "10969acc-0105-49fe-bea3-4618ccf29d2a")
    String id,

    @Schema(description = "Authority name", example = "ROLE_ADMIN")
    String name,

    @Schema(
        description = "Authority description",
        example = "Administrator with full access"
    )
    String description,

    @JsonProperty("protected")
    @Schema(
        description = "Whether this authority is protected (cannot be deleted)",
        example = "true"
    )
    Boolean protectedFlag,

    @Schema(description = "Timestamp when the authority was created", example = "2025-06-13T15:47:38.786Z")
    Instant createdAt,

    @Schema(description = "Timestamp when the authority was last updated", example = "2025-06-13T16:00:00.000Z")
    Instant updatedAt
) {}
