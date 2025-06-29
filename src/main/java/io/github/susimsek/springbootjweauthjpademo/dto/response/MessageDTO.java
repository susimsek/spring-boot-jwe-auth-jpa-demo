package io.github.susimsek.springbootjweauthjpademo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

import java.time.Instant;

@Schema(name = "Message", description = "Represents an internationalization message entry")
@Relation(collectionRelation="messages", itemRelation="message")
public record MessageDTO(
    @Schema(description = "Message ID", example = "10969acc-0105-49fe-bea3-4618ccf29d2a")
    String id,

    @Schema(description = "Locale code", example = "en")
    String locale,

    @Schema(description = "Message key/code", example = "jakarta.validation.constraints.NotBlank.message")
    String code,

    @Schema(description = "Message content", example = "Field cannot be blank.")
    String content,

    @Schema(description = "Timestamp when message was created", example = "2025-05-10T12:00:00Z")
    Instant createdAt,

    @Schema(description = "Timestamp when message was last updated", example = "2025-05-10T12:00:00Z")
    Instant updatedAt
) {}
