package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ConfirmTotpRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.MfaStatusDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PasswordVerifyRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TotpDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpDTO;
import io.github.susimsek.springbootjweauthjpademo.service.MfaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/account/mfa")
@RequiredArgsConstructor
@Tag(name = "mfa", description = "Endpoints for managing Multi-Factor Authentication")
@Slf4j
public class MfaController {

    private final MfaService mfaService;

    @Operation(
        summary     = "Get MFA status",
        description = "Retrieve whether MFA is enabled and verified for the current user",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "MFA status returned",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = MfaStatusDTO.class)
                )
            )
        }
    )
    @GetMapping(
        path     = "/status",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<MfaStatusDTO>> getMfaStatus(@AuthenticationPrincipal Jwt jwt) {
        log.debug("REST request to get MFA status for user : {}", jwt.getSubject());
        String username = jwt.getSubject();
        MfaStatusDTO status = mfaService.getMfaStatus(username);
        EntityModel<MfaStatusDTO> model = EntityModel.of(status,
            linkTo(methodOn(MfaController.class).getMfaStatus(jwt)).withSelfRel(),
            linkTo(methodOn(MfaController.class).setupMfa(jwt, null)).withRel("setup"),
            linkTo(methodOn(MfaController.class).confirmMfa(jwt, null)).withRel("confirm"),
            linkTo(methodOn(MfaController.class).disableMfa(jwt, null)).withRel("disable"),
            linkTo(methodOn(MfaController.class).regenerateMfa(jwt, null)).withRel("regenerate")
        );
        return ResponseEntity.ok(model);
    }

    @RateLimiter(name = "mfaSetup", key = "#jwt.subject")
    @Operation(
        summary     = "Initialize MFA setup",
        description = "Verify password and generate TOTP secret and QR code URL",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "TOTP details returned",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = TotpDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid password",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/setup",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<TotpDTO>> setupMfa(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload containing current password",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = PasswordVerifyRequestDTO.class)
            )
        )
        @RequestBody @Valid PasswordVerifyRequestDTO request
    ) {
        log.debug("REST request to setup MFA for user : {}", jwt.getSubject());
        String username = jwt.getSubject();
        TotpDTO totpInfo = mfaService.setupMfa(username, request.password());
        EntityModel<TotpDTO> model = EntityModel.of(totpInfo,
            linkTo(methodOn(MfaController.class).getMfaStatus(jwt)).withRel("status")
        );
        return ResponseEntity.ok(model);
    }

    @RateLimiter(name = "mfaConfirm", key = "#jwt.subject")
    @Operation(
        summary     = "Confirm MFA setup",
        description = "Verify TOTP code to complete MFA setup",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "MFA confirmed",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = VerifyTotpDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid or expired TOTP code",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "409", description = "MFA already verified or not enabled",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/confirm",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<VerifyTotpDTO>> confirmMfa(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload containing the 6-digit TOTP code",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = ConfirmTotpRequestDTO.class)
            )
        )
        @RequestBody @Valid ConfirmTotpRequestDTO request
    ) {
        log.debug("REST request to confirm MFA for user : {}", jwt.getSubject());
        String username = jwt.getSubject();
        VerifyTotpDTO dto = mfaService.confirmMfa(username, request);
        EntityModel<VerifyTotpDTO> model = EntityModel.of(dto,
            linkTo(methodOn(MfaController.class).getMfaStatus(jwt)).withRel("status")
        );
        return ResponseEntity.ok(model);
    }

    @RateLimiter(name = "mfaDisable", key = "#jwt.subject")
    @Operation(
        summary     = "Disable MFA",
        description = "Disable two-factor authentication after verifying password",
        responses   = {
            @ApiResponse(
                responseCode = "204",
                description  = "MFA disabled",
                content      = @Content
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid password",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "409", description = "MFA already disabled",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/disable",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> disableMfa(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload containing current password",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = PasswordVerifyRequestDTO.class)
            )
        )
        @RequestBody @Valid PasswordVerifyRequestDTO request
    ) {
        log.debug("REST request to disable MFA for user : {}", jwt.getSubject());
        String username = jwt.getSubject();
        mfaService.disableMfa(username, request.password());
        return ResponseEntity.noContent().build();
    }

    @RateLimiter(name = "mfaRegenerate", key = "#jwt.subject")
    @Operation(
        summary     = "Regenerate MFA secret",
        description = "Generate a new TOTP secret and require re-verification",
        responses   = {
            @ApiResponse(
                responseCode = "200",
                description  = "New TOTP details returned",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = TotpDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Invalid password",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(
                responseCode = "409", description = "MFA not enabled",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/regenerate",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<TotpDTO>> regenerateMfa(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload containing current password",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = PasswordVerifyRequestDTO.class)
            )
        )
        @RequestBody  @Valid PasswordVerifyRequestDTO pwdDto) {
        log.debug("REST request to regenerate MFA for user : {}", jwt.getSubject());
        TotpDTO totpInfo = mfaService.regenerateMfa(jwt.getSubject(), pwdDto.password());
        EntityModel<TotpDTO> model = EntityModel.of(totpInfo,
            linkTo(methodOn(MfaController.class).getMfaStatus(jwt)).withRel("status")
        );
        return ResponseEntity.ok(model);
    }

}
