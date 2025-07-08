package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.dto.response.AvatarUploadDTO;
import io.github.susimsek.springbootjweauthjpademo.service.AvatarService;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Image;
import io.github.susimsek.springbootjweauthjpademo.validation.annotation.NotEmptyFile;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/admin/users/{userId}/avatar")
@RequiredArgsConstructor
@Validated
@Tag(name = "admin-avatars", description = "Admin endpoints for managing user avatars")
@Slf4j
public class AdminAvatarController {

    private final AvatarService avatarService;

    @Operation(
        summary     = "Upload new avatar",
        description = "Upload a new avatar image for the specified user (admin action).",
        responses   = {
            @ApiResponse(responseCode = "200", description = "Avatar uploaded",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                   schema = @Schema(implementation = AvatarUploadDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                   schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "413", description = "Payload Too Large")
        }
    )
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<AvatarUploadDTO>> uploadAvatar(
        @Parameter(description = "ID of the user to upload avatar for")
        @PathVariable
        String userId,

        @Parameter(description = "Image file to upload")
        @RequestPart("file")
        @Valid @NotEmptyFile @Image
        MultipartFile file
    ) {
        log.debug("REST request to upload avatar for user {} with file {}", userId, file.getOriginalFilename());
        AvatarUploadDTO dto = avatarService.uploadAvatar(userId, file);
        EntityModel<AvatarUploadDTO> model = EntityModel.of(dto,
            linkTo(methodOn(AvatarController.class).getAvatar(userId, null)).withSelfRel(),
            linkTo(methodOn(AdminAvatarController.class).deleteAvatar(userId)).withRel("delete").withType("delete")
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary     = "Delete avatar",
        description = "Delete the avatar for the specified user (admin action).",
        responses   = {
            @ApiResponse(responseCode = "204", description = "Avatar deleted")
        }
    )
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteAvatar(
        @PathVariable
        @UUID
        String userId
    ) {
        log.debug("REST request to delete avatar for user {}", userId);
        avatarService.deleteAvatar(userId);
        return ResponseEntity.noContent().build();
    }
}
