package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.dto.response.AvatarDTO;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Validated
@Tag(name = "avatar", description = "Endpoints for managing user avatars")
@Slf4j
public class AvatarController {

    private final AvatarService avatarService;

    @Operation(
        summary = "Get user avatar",
        description = "Return the avatar image for the authenticated user.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "Avatar image returned",
                content = @Content(
                    mediaType = MediaType.IMAGE_PNG_VALUE,
                    schema    = @Schema(implementation = AvatarDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "304",
                description  = "Not modified",
                content      = @Content
            ),
            @ApiResponse(responseCode = "400", description = "Invalid userId format",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description  = "Unauthorized",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "403",
                description  = "Forbidden",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "404",
                description  = "Avatar not found",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @GetMapping(value = "/{userId}/avatar",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getAvatar(
        @Parameter(description = "The identifier of the user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @PathVariable
        String userId,
        HttpServletRequest request
    ) {
        log.debug("REST request to get avatar for user : {}", userId);

        AvatarDTO dto = avatarService.getAvatar(userId);
        String etag = "\"" + dto.contentHash() + "\"";
        long lastModified = dto.lastModified().toEpochMilli();

        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        long ifModifiedSince = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);

        if (ifNoneMatch != null && ifNoneMatch.equals(etag) ||
            ifModifiedSince > 0 && ifModifiedSince >= lastModified) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .build();
        }

        return ResponseEntity.ok()
            .eTag(etag)
            .lastModified(lastModified)
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
            .contentType(MediaType.parseMediaType(dto.contentType()))
            .body(dto.content());
    }

    @Operation(
        summary = "Upload new avatar",
        description = "Upload a new avatar image for the authenticated user.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "Avatar uploaded",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = AvatarUploadDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid file",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description  = "Unauthorized",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "413", description = "Payload Too Large",
                content = @Content
            ),
        }
    )
    @PutMapping(
        value    = "/avatar",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<AvatarUploadDTO>> uploadAvatar(
        @AuthenticationPrincipal Jwt jwt,

        @Parameter(description = "Image file to upload")
        @RequestPart("file")
        @Valid @NotEmptyFile @Image
        MultipartFile file
    ) {
        String userId = jwt.getSubject();
        log.debug("REST request to upload avatar for user {} with file {}", userId, file.getOriginalFilename());
        AvatarUploadDTO dto = avatarService.uploadAvatar(userId, file);
        EntityModel<AvatarUploadDTO> resource = EntityModel.of(dto,
            linkTo(methodOn(AvatarController.class).getAvatar(userId, null)).withSelfRel(),
            linkTo(methodOn(AvatarController.class).deleteAvatar(null)).withRel("delete")
        );
        return ResponseEntity.ok(resource);
    }

    @Operation(
        summary = "Delete avatar",
        description = "Delete the authenticated user's avatar.",
        responses   = {
            @ApiResponse(
                responseCode = "204",
                description  = "Avatar deleted",
                content      = @Content
            ),
            @ApiResponse(
                responseCode = "401",
                description  = "Unauthorized",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @DeleteMapping(
        path     = "/avatar",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> deleteAvatar(
        @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getSubject();
        log.debug("REST request to delete avatar for user : {}", userId);
        avatarService.deleteAvatar(userId);
        return ResponseEntity.noContent().build();
    }
}
