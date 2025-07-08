package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.dto.filter.MessageFilter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateMessageRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.MessageDTO;
import io.github.susimsek.springbootjweauthjpademo.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
@Validated
@Tag(name = "messages", description = "Admin operations for message management")
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final PagedResourcesAssembler<MessageDTO> assembler;

    @Operation(
        summary = "List messages with pagination",
        description = "Get paginated list of messages",
        responses = {
            @ApiResponse(responseCode = "200", description = "Page of messages returned",
                content = @Content(schema = @Schema(implementation = PagedModel.class)))
        }
    )
    @GetMapping(params = {"page","size"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<EntityModel<MessageDTO>>> listMessagesPaged(
        @ParameterObject Pageable pageable,
        @ParameterObject @Valid MessageFilter filter
    ) {
        log.debug("REST request to list messages paged: pageable={}, filter={}", pageable, filter);
        Page<MessageDTO> page = messageService.listMessagesPaged(pageable, filter);
        PagedModel<EntityModel<MessageDTO>> model = assembler.toModel(
            page,
            dto -> EntityModel.of(dto,
                linkTo(methodOn(MessageController.class).getMessage(dto.id())).withSelfRel(),
                linkTo(methodOn(MessageController.class).updateMessage(dto.id(), null)).withRel("update"),
                linkTo(methodOn(MessageController.class).deleteMessage(dto.id())).withRel("delete")
            )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Get message by ID", description = "Retrieve a message by its ID", responses = {
        @ApiResponse(responseCode = "200", description = "Message found",
            content = @Content(schema = @Schema(implementation = MessageDTO.class))),
        @ApiResponse(responseCode = "404", description = "Message not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping(path = "/{messageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<MessageDTO>> getMessage(
        @PathVariable("messageId")
        @UUID
        String messageId
    ) {
        log.debug("REST request to get message : {}", messageId);
        MessageDTO dto = messageService.getMessage(messageId);
        EntityModel<MessageDTO> model = EntityModel.of(dto,
            linkTo(methodOn(MessageController.class).getMessage(messageId)).withSelfRel(),
            linkTo(methodOn(MessageController.class).updateMessage(messageId, null)).withRel("update"),
            linkTo(methodOn(MessageController.class).deleteMessage(messageId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Create a new message", responses = {
        @ApiResponse(responseCode = "201", description = "Message created"),
        @ApiResponse(responseCode = "409", description = "Code conflict",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<MessageDTO>> createMessage(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for creating a new message",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CreateMessageRequestDTO.class)
            )
        )
        @RequestBody @Valid CreateMessageRequestDTO req
    ) {
        log.debug("REST request to create message : {}", req);
        MessageDTO created = messageService.createMessage(req);
        EntityModel<MessageDTO> model = EntityModel.of(created,
            linkTo(methodOn(MessageController.class).getMessage(created.id())).withSelfRel(),
            linkTo(methodOn(MessageController.class).updateMessage(created.id(), null)).withRel("update"),
            linkTo(methodOn(MessageController.class).deleteMessage(created.id())).withRel("delete")
        );
        URI location = model.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "Update an existing message", responses = {
        @ApiResponse(responseCode = "200", description = "Message updated"),
        @ApiResponse(responseCode = "404", description = "Message not found"),
        @ApiResponse(responseCode = "409", description = "Code conflict")
    })
    @PutMapping(path = "/{messageId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<MessageDTO>> updateMessage(
        @PathVariable("messageId")
        @UUID String messageId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for updating an existing message",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UpdateMessageRequestDTO.class)
            )
        )
        @RequestBody @Valid UpdateMessageRequestDTO req
    ) {
        log.debug("REST request to update message {} : {}", messageId, req);
        MessageDTO updated = messageService.updateMessage(messageId, req);
        EntityModel<MessageDTO> model = EntityModel.of(updated,
            linkTo(methodOn(MessageController.class).getMessage(messageId)).withSelfRel(),
            linkTo(methodOn(MessageController.class).updateMessage(messageId, null)).withRel("update"),
            linkTo(methodOn(MessageController.class).deleteMessage(messageId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Partially update a message", responses = {
        @ApiResponse(responseCode = "200", description = "Message updated"),
        @ApiResponse(responseCode = "404", description = "Message not found"),
        @ApiResponse(responseCode = "409", description = "Code conflict")
    })
    @PatchMapping(path = "/{messageId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<MessageDTO>> patchMessage(
        @PathVariable("messageId") @UUID String messageId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for partially updating a message",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PartialUpdateMessageRequestDTO.class)
            )
        )
        @RequestBody @Valid PartialUpdateMessageRequestDTO req
    ) {
        log.debug("REST request to patch message {} : {}", messageId, req);
        MessageDTO updated = messageService.patchMessage(messageId, req);
        EntityModel<MessageDTO> model = EntityModel.of(updated,
            linkTo(methodOn(MessageController.class).getMessage(messageId)).withSelfRel(),
            linkTo(methodOn(MessageController.class).updateMessage(messageId, null)).withRel("update"),
            linkTo(methodOn(MessageController.class).deleteMessage(messageId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Delete a message", responses = {
        @ApiResponse(responseCode = "204", description = "Message deleted"),
        @ApiResponse(responseCode = "404", description = "Message not found")
    })
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
        @PathVariable("messageId")
        @UUID
        String messageId
    ) {
        log.debug("REST request to delete message : {}", messageId);
        messageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }
}
