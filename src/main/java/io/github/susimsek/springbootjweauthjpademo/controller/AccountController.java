package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ChangeEmailRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ChangePasswordRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.DeleteAccountRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateProfileRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ChangeEmailDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ChangePasswordDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ProfileDTO;
import io.github.susimsek.springbootjweauthjpademo.security.CookieUtils;
import io.github.susimsek.springbootjweauthjpademo.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "account", description = "Endpoints for managing user account")
public class AccountController {

    private final AccountService accountService;

    @RateLimiter(name = "changePassword", key = "#jwt.subject")
    @Operation(
        summary     = "Change user password",
        description = "Update the authenticated user's password",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "Password changed successfully",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ChangePasswordDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid request",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path = "/change-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChangePasswordDTO> changePassword(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Current and new password payload",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = ChangePasswordRequestDTO.class)
            )
        )
        @RequestBody @Valid ChangePasswordRequestDTO changePasswordRequest
    ) {
        log.debug("REST request to change password : {}", jwt.getSubject());
        String username = jwt.getSubject();
        ChangePasswordDTO response = accountService.changePassword(username, changePasswordRequest);
        ResponseCookie deleteRefresh = CookieUtils.removeRefreshTokenCookie();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
            .body(response);
    }

    @RateLimiter(name = "changeEmail", key = "#jwt.subject")
    @Operation(
        summary     = "Change user email",
        description = "Request email change; sends verification to new email",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "Verification email sent",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ChangeEmailDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid request",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path = "/change-email",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ChangeEmailDTO> changeEmail(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Current password and new email payload",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = ChangeEmailRequestDTO.class)
            )
        )
        @RequestBody @Valid  ChangeEmailRequestDTO changeEmailRequest
    ) {
        log.debug("REST request to change email : {}", changeEmailRequest);
        String username = jwt.getSubject();
        ChangeEmailDTO response = accountService.changeEmail(username, changeEmailRequest);
        return ResponseEntity.ok(response);
    }

    @RateLimiter(name = "confirmEmailChange", key = "#token")
    @Operation(
        summary     = "Confirm email change",
        description = "Finalize email change using verification token",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "Email change confirmed",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ChangeEmailDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid or expired token",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "409", description = "No pending email change",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @GetMapping(path = "/confirm-email", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChangeEmailDTO> confirmEmailChange(
        @Parameter(
            description = "Email change verification token",
            example     = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        @RequestParam @NotBlank(message = "Field is required.") String token
    ) {
        log.debug("REST request to confirm email change : {}", token);
        ChangeEmailDTO response = accountService.confirmEmailChange(token);

        ResponseCookie removeAccess = CookieUtils.removeAccessTokenCookie();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, removeAccess.toString())
            .body(response);
    }

    @Operation(
        summary     = "Get user profile",
        description = "Retrieve the authenticated user's profile information",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Profile returned",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProfileDTO.class)
                )
            )
        }
    )
    @GetMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProfileDTO>> getProfile(
        @AuthenticationPrincipal Jwt jwt) {
        log.debug("REST request to get profile for user : {}", jwt.getSubject());
        ProfileDTO dto = accountService.getProfile(jwt.getSubject());
        EntityModel<ProfileDTO> model = EntityModel.of(dto,
            linkTo(methodOn(AccountController.class).getProfile(jwt)).withSelfRel(),
            linkTo(methodOn(AccountController.class).updateProfile(jwt, null))
                .withRel("update")
                .withType(HttpMethod.PUT.name()),
            linkTo(methodOn(AvatarController.class).uploadAvatar(null, null))
                .withRel("upload-avatar")
                .withType(HttpMethod.PUT.name()),
            linkTo(methodOn(AvatarController.class).deleteAvatar(null))
                .withRel("delete-avatar")
                .withType(HttpMethod.DELETE.name()),
            linkTo(methodOn(AccountController.class).deleteAccount(null, null))
                .withRel("delete-account")
                .withType(HttpMethod.DELETE.name())
        );
        return ResponseEntity.ok(model);
    }

    @Operation(
        summary     = "Update user profile",
        description = "Modify the authenticated user's profile",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "Profile updated",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProfileDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400", description = "Invalid request",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PutMapping(
        path = "/profile",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<ProfileDTO>> updateProfile(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload to update in user profile",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = UpdateProfileRequestDTO.class)
            )
        )
        @RequestBody @Valid UpdateProfileRequestDTO dtoRequest
    ) {
        log.debug("REST request to update profile : {}", dtoRequest);
        ProfileDTO dto = accountService.updateProfile(jwt.getSubject(), dtoRequest);
        EntityModel<ProfileDTO> model = EntityModel.of(dto,
            linkTo(methodOn(AccountController.class).getProfile(jwt)).withSelfRel(),
            linkTo(methodOn(AccountController.class).updateProfile(jwt, null))
                .withRel("update")
                .withType(HttpMethod.PUT.name()),
            linkTo(methodOn(AvatarController.class).uploadAvatar((Jwt) null, null))
                .withRel("upload-avatar")
                .withType(HttpMethod.PUT.name()),
            linkTo(methodOn(AvatarController.class).deleteAvatar((Jwt) null))
                .withRel("delete-avatar")
                .withType(HttpMethod.DELETE.name()),
            linkTo(methodOn(AccountController.class).deleteAccount((Jwt) null, null))
                .withRel("delete-account")
                .withType(HttpMethod.DELETE.name())
        );
        return ResponseEntity.ok(model);
    }

    @RateLimiter(name = "deleteAccount", key = "#jwt.subject")
    @Operation(
        summary     = "Delete user account",
        description = "Delete the authenticated user's account",
        responses   = {
            @ApiResponse(responseCode = "204", description = "Account deleted successfully")
        }
    )
    @DeleteMapping(
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> deleteAccount(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload to confirm account deletion",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = DeleteAccountRequestDTO.class)
            )
        )
        @RequestBody @Valid DeleteAccountRequestDTO dto
    ) {
        log.debug("REST request to delete account : {}", jwt.getSubject());
        String username = jwt.getSubject();
        accountService.deleteAccount(username, dto);
        ResponseCookie deleteRefresh = CookieUtils.removeRefreshTokenCookie();
        ResponseCookie deleteAccess  = CookieUtils.removeAccessTokenCookie();
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString(), deleteAccess.toString())
            .build();
    }
}
