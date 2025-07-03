package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.dto.filter.UserFilter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.UserDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceAlreadyExistsException;
import io.github.susimsek.springbootjweauthjpademo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "admin-users", description = "Admin operations for user management")
@Slf4j
public class UserController {

    private final UserService userService;

    private final PagedResourcesAssembler<UserDTO> pagedResourcesAssembler;

    @Operation(
        summary = "List users",
        description = "Get a paginated list of users",
        responses = {
            @ApiResponse(responseCode = "200", description = "Page of users returned",
                content = @Content(schema = @Schema(implementation = PagedModel.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PagedModel<EntityModel<UserDTO>>> listUsers(
        @ParameterObject Pageable pageable,
        @ParameterObject @Valid UserFilter filter
    ) {
        log.debug("REST request to list users paged: pageable={}, filter={}", pageable, filter);
        Page<UserDTO> result = userService.listUsers(pageable, filter);
        PagedModel<EntityModel<UserDTO>> pageModel = pagedResourcesAssembler.toModel(
            result,
            dto -> EntityModel.of(dto,
                linkTo(methodOn(UserController.class).getUser(dto.id())).withSelfRel(),
                linkTo(methodOn(UserController.class).updateUser(dto.id(), null)).withRel("update"),
                linkTo(methodOn(UserController.class).deleteUser(null, dto.id())).withRel("delete")
            )
        );
        return ResponseEntity.ok(pageModel);
    }

    @Operation(
        summary = "Get user by ID",
        description = "Retrieve a single user by its ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UserDTO>> getUser(
        @Parameter(description = "ID of the user to retrieve")
        @PathVariable("userId")
        @UUID
        String userId
    ) {
        log.debug("REST request to get user : {}", userId);
        UserDTO dto = userService.getUser(userId);
        EntityModel<UserDTO> model = EntityModel.of(dto,
            linkTo(methodOn(UserController.class).getUser(userId)).withSelfRel(),
            linkTo(methodOn(UserController.class).updateUser(userId, null)).withRel("update"),
            linkTo(methodOn(UserController.class).deleteUser(null, userId)).withRel("delete"),
            linkTo(methodOn(AdminAvatarController.class).uploadAvatar(userId, null))
                .withRel("upload-avatar")
                .withType(HttpMethod.PUT.name()),
            linkTo(methodOn(AdminAvatarController.class).deleteAvatar(userId))
                .withRel("delete-avatar")
                .withType(HttpMethod.DELETE.name())
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Create a new user", description = "Create a new user with given details",
        responses = {
            @ApiResponse(responseCode = "201", description = "User created",
                content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Username or email conflict",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UserDTO>> createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for creating a new user",
            required    = true,
            content     = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(implementation = CreateUserRequestDTO.class)
            )
        )
        @RequestBody @Valid CreateUserRequestDTO req
    ) {
        log.debug("REST request to create user : {}", req);
        UserDTO created = userService.createUser(req);
        EntityModel<UserDTO> model = EntityModel.of(created,
            linkTo(methodOn(UserController.class).getUser(created.id())).withSelfRel(),
            linkTo(methodOn(UserController.class).updateUser(created.id(), null)).withRel("update"),
            linkTo(methodOn(UserController.class).deleteUser(null, created.id())).withRel("delete")
        );
        URI location = model.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(
        summary = "Update an existing user",
        description = "Update user details by ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "User updated",
                content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Username or email conflict",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @PutMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UserDTO>> updateUser(
        @Parameter(description = "ID of the user to update")
        @PathVariable("userId")
        @UUID
        String userId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for updating user details",
            required    = true,
            content     = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema    = @Schema(implementation = UpdateUserRequestDTO.class)
            )
        )
        @RequestBody @Valid UpdateUserRequestDTO req
    ) {
        log.debug("REST request to update user {} : {}", userId, req);
        UserDTO updated = userService.updateUser(userId, req);
        EntityModel<UserDTO> model = EntityModel.of(updated,
            linkTo(methodOn(UserController.class).getUser(userId)).withSelfRel(),
            linkTo(methodOn(UserController.class).updateUser(userId, null)).withRel("update"),
            linkTo(methodOn(UserController.class).deleteUser(null, userId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary = "Partially update a user",
        description = "Update one or more fields of a user",
        responses = {
            @ApiResponse(responseCode = "200", description = "User updated",
                content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Username or email already in use",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @PatchMapping(path = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UserDTO>> patchUser(
        @Parameter(description = "ID of the user to patch")
        @PathVariable("userId")
        @UUID
        String userId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload for partial user update",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PartialUpdateUserRequestDTO.class)
            )
        )
        @RequestBody @Valid PartialUpdateUserRequestDTO req
    ) {
        log.debug("REST request to patch user {} : {}", userId, req);
        UserDTO updated = userService.patchUser(userId, req);
        EntityModel<UserDTO> model = EntityModel.of(updated,
            linkTo(methodOn(UserController.class).getUser(userId)).withSelfRel(),
            linkTo(methodOn(UserController.class).updateUser(userId, null)).withRel("update"),
            linkTo(methodOn(UserController.class).deleteUser(null, userId)).withRel("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Delete a user",
        description = "Remove a user by its ID",
        responses = {
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Cannot delete own account",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    @DeleteMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteUser(
        @AuthenticationPrincipal Jwt jwt,
        @Parameter(description = "ID of the user to delete")
        @PathVariable("userId")
        @UUID
        String userId
    ) {
        log.debug("REST request to delete user : {}", userId);
        if (jwt.getSubject().equals(userId)) {
            throw new ResourceAlreadyExistsException(
                ProblemType.SELF_DELETION_NOT_ALLOWED,
                "userId",
                userId
            );
        }
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
