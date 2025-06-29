package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.dto.filter.AuthorityFilter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateAuthorityRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.AuthorityDTO;
import io.github.susimsek.springbootjweauthjpademo.service.AuthorityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/admin/authorities")
@RequiredArgsConstructor
@Validated
@Tag(name = "authorities", description = "Admin operations for authority management")
@Slf4j
public class AuthorityController {
    private final AuthorityService authorityService;
    private final PagedResourcesAssembler<AuthorityDTO> assembler;

    @Operation(
        summary     = "List all authorities",
        description = "Returns a list of all available authorities",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "List of authorities returned successfully",
                content      = @Content(
                    mediaType    = MediaType.APPLICATION_JSON_VALUE,
                    array        = @ArraySchema(schema = @Schema(implementation = AuthorityDTO.class))
                )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AuthorityDTO>> getAllAuthorities() {
        log.debug("REST request to get all authorities");
        return ResponseEntity.ok(authorityService.getAllAuthorities());
    }

    @Operation(
        summary = "List authorities with pagination",
        description = "Get a paginated list of authorities",
        responses = {
            @ApiResponse(responseCode = "200", description = "Page of authorities returned",
                content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, params = {"page", "size"})
    public ResponseEntity<PagedModel<EntityModel<AuthorityDTO>>> listAuthoritiesPaged(
        @ParameterObject Pageable pageable,
        @ParameterObject @Valid AuthorityFilter filter
    ) {
        log.debug("REST request to list authorities paged: pageable={}, filter={}", pageable, filter);
        Page<AuthorityDTO> result = authorityService.listAuthoritiesPaged(pageable, filter);
        PagedModel<EntityModel<AuthorityDTO>> model = assembler.toModel(
            result,
            dto -> EntityModel.of(dto,
                linkTo(methodOn(AuthorityController.class).getAuthority(dto.id())).withSelfRel(),
                linkTo(methodOn(AuthorityController.class).updateAuthority(dto.id(), null)).withRel("update"),
                linkTo(methodOn(AuthorityController.class).deleteAuthority(dto.id())).withRel("delete")
            )
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Get authority by ID",
        description = "Retrieve a single authority by its ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Authority found",
                content = @Content(schema = @Schema(implementation = AuthorityDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Authority not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @GetMapping(path = "/{authorityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<AuthorityDTO>> getAuthority(
        @Parameter(description = "ID of the authority to retrieve")
        @PathVariable("authorityId")
        @UUID
        String authorityId
    ) {
        log.debug("REST request to get authority : {}", authorityId);
        AuthorityDTO dto = authorityService.getAuthority(authorityId);
        EntityModel<AuthorityDTO> model = EntityModel.of(dto,
            linkTo(methodOn(AuthorityController.class).getAuthority(authorityId)).withSelfRel(),
            linkTo(methodOn(AuthorityController.class).updateAuthority(authorityId, null)).withRel("update"),
            linkTo(methodOn(AuthorityController.class).deleteAuthority(authorityId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Create a new authority",
        responses = {
            @ApiResponse(responseCode = "201", description = "Authority created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Name conflict",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<AuthorityDTO>> createAuthority(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for creating a new authority",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = CreateAuthorityRequestDTO.class)
            )
        )
        @RequestBody @Valid CreateAuthorityRequestDTO req
    ) {
        log.debug("REST request to create authority : {}", req);
        AuthorityDTO created = authorityService.createAuthority(req);
        EntityModel<AuthorityDTO> model = EntityModel.of(created,
            linkTo(methodOn(AuthorityController.class).getAuthority(created.id())).withSelfRel(),
            linkTo(methodOn(AuthorityController.class).updateAuthority(created.id(), null)).withRel("update"),
            linkTo(methodOn(AuthorityController.class).deleteAuthority(created.id())).withRel("delete")
        );
        URI location = model.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(
        summary = "Update an existing authority",
        responses = {
            @ApiResponse(responseCode = "200", description = "Authority updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Authority not found"),
            @ApiResponse(responseCode = "409", description = "Name conflict",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @PutMapping(path = "/{authorityId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<AuthorityDTO>> updateAuthority(
        @PathVariable("authorityId")
        @UUID
        String authorityId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for updating an existing authority",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UpdateAuthorityRequestDTO.class)
            )
        )
        @RequestBody @Valid UpdateAuthorityRequestDTO req
    ) {
        log.debug("REST request to update authority {} : {}", authorityId, req);
        AuthorityDTO updated = authorityService.updateAuthority(authorityId, req);
        EntityModel<AuthorityDTO> model = EntityModel.of(updated,
            linkTo(methodOn(AuthorityController.class).getAuthority(authorityId)).withSelfRel(),
            linkTo(methodOn(AuthorityController.class).updateAuthority(authorityId, null)).withRel("update"),
            linkTo(methodOn(AuthorityController.class).deleteAuthority(authorityId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Partially update an authority",
        responses = {
            @ApiResponse(responseCode = "200", description = "Authority updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Authority not found"),
            @ApiResponse(responseCode = "409", description = "Name conflict",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @PatchMapping(path = "/{authorityId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<AuthorityDTO>> patchAuthority(
        @PathVariable("authorityId")
        @UUID
        String authorityId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for partially updating an authority",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PartialUpdateAuthorityRequestDTO.class)
            )
        )
        @RequestBody @Valid PartialUpdateAuthorityRequestDTO req
    ) {
        log.debug("REST request to patch authority {} : {}", authorityId, req);
        AuthorityDTO patched = authorityService.patchAuthority(authorityId, req);
        EntityModel<AuthorityDTO> model = EntityModel.of(patched,
            linkTo(methodOn(AuthorityController.class).getAuthority(authorityId)).withSelfRel(),
            linkTo(methodOn(AuthorityController.class).updateAuthority(authorityId, null)).withRel("update"),
            linkTo(methodOn(AuthorityController.class).deleteAuthority(authorityId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Delete an authority",
        responses = {
            @ApiResponse(responseCode = "204", description = "Authority deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Protected authority",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Authority not found")
        }
    )
    @DeleteMapping("/{authorityId}")
    public ResponseEntity<Void> deleteAuthority(
        @PathVariable("authorityId")
        @UUID
        String authorityId
    ) {
        log.debug("REST request to delete authority : {}", authorityId);
        authorityService.deleteAuthority(authorityId);
        return ResponseEntity.noContent().build();
    }
}
