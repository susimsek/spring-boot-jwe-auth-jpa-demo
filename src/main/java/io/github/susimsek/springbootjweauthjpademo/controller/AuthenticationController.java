package io.github.susimsek.springbootjweauthjpademo.controller;
import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiter;

import io.github.susimsek.springbootjweauthjpademo.dto.request.ConfirmTotpRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.EmailVerificationDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ForgotPasswordDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ForgotPasswordRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.LoginRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.LoginResultDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RefreshTokenResultDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.RegisterRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ResetPasswordDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ResetPasswordRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TotpDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RegistrationDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.VerifyTotpRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpResultDTO;
import io.github.susimsek.springbootjweauthjpademo.security.CookieUtils;
import io.github.susimsek.springbootjweauthjpademo.service.AuthenticationService;
import io.github.susimsek.springbootjweauthjpademo.service.MfaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "auth", description = "Endpoints for managing authentication")
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final MfaService mfaService;

    @RateLimiter(name = "register", key = "#registerRequest.email")
    @Operation(
        summary     = "Register a new user",
        description = "Creates a new user account.",
        responses = {
            @ApiResponse(responseCode = "201", description = "User successfully registered",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = RegistrationDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "409", description = "Username or email already in use",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @SecurityRequirements
    @PostMapping(
        path     = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegistrationDTO> register(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Registration request payload",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = RegisterRequestDTO.class)
            )
        )
       @RequestBody @Valid RegisterRequestDTO registerRequest
    ) {
        log.debug("REST request to register user : {}", registerRequest.username());
        RegistrationDTO created = authenticationService.register(registerRequest);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created);
    }

    @RateLimiter(name = "login", key  = "#request.username")
    @Operation(
        summary     = "Login user",
        description = "Authenticate username/password.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = LoginResultDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid login request",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "403", description = "MFA setup required",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @SecurityRequirements
    @PostMapping(
        path     = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<LoginResultDTO>> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Login request payload",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = LoginRequestDTO.class)
            )
        )
        @RequestBody @Valid LoginRequestDTO request) {
        log.debug("REST request to login user : {}", request.username());
        LoginResultDTO loginResult = authenticationService.login(request.username(), request.password());

        EntityModel<LoginResultDTO> model = EntityModel.of(loginResult);
        model.add(linkTo(methodOn(AuthenticationController.class).login(request)).withSelfRel());
        model.add(linkTo(methodOn(AuthenticationController.class).refreshToken(null)).withRel("refresh-token"));
        model.add(linkTo(methodOn(AuthenticationController.class).logout(null)).withRel("logout"));

        if (loginResult.mfaToken() != null) {
            ResponseCookie mfaCookie = CookieUtils.createMfaTokenCookie(loginResult.mfaToken());
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, mfaCookie.toString())
                .body(model);
        }
        // Normal login: accessToken var
        ResponseCookie accessCookie = CookieUtils.createAccessTokenCookie(loginResult.accessToken());
        ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(loginResult.refreshToken());
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(model);
    }

    @RateLimiter(name = "refreshToken", key = "#jwt.subject")
    @Operation(
        summary     = "Refresh access and refresh tokens",
        description = "Exchange a valid refresh token for new access and refresh tokens.",
        responses = {
            @ApiResponse(
                responseCode = "200", description = "New tokens issued",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = RefreshTokenResultDTO.class)
                )
            ),
            @ApiResponse(
                responseCode = "401", description = "Invalid or expired refresh token",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/refresh-token",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RefreshTokenResultDTO> refreshToken(
        @AuthenticationPrincipal Jwt jwt
    ) {

        log.debug("REST request to refresh token for user : {}", jwt.getSubject());
        RefreshTokenResultDTO result = authenticationService.refreshToken(jwt);

        ResponseCookie accessCookie  = CookieUtils.createAccessTokenCookie(result.accessToken());
        ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(result.refreshToken());

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .body(result);
    }


    @Operation(
        summary     = "Initiate TOTP setup",
        description = "Begin two-factor authentication setup",
        responses = {
            @ApiResponse(responseCode = "200", description = "TOTP setup info returned",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = TotpDTO.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/setup-totp",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TotpDTO> setupTotp(
        @AuthenticationPrincipal Jwt jwt
    ) {
        log.debug("REST request to setup TOTP for user : {}", jwt.getSubject());
        TotpDTO totpDto = mfaService.setupMfa(jwt.getSubject());
        return ResponseEntity.ok(totpDto);
    }

    @RateLimiter(name = "confirmTotp", key = "#jwt.subject")
    @Operation(
        summary     = "Confirm TOTP code",
        description = "Verify the TOTP code during registration or login",
        responses = {
            @ApiResponse(responseCode = "200", description = "TOTP confirmed",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = VerifyTotpDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/confirm-totp",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VerifyTotpDTO> confirmTotp(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "TOTP confirmation payload including flow (registration or login)",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = ConfirmTotpRequestDTO.class)
            )
        )
        @RequestBody @Valid ConfirmTotpRequestDTO request
    ) {
        log.debug("REST request to confirm TOTP for user : {}", jwt.getSubject());
        String username = jwt.getSubject();
        VerifyTotpDTO dto = mfaService.confirmMfa(username, request);

        ResponseCookie removeMfaCookie = CookieUtils.removeMfaTokenCookie();

        ResponseEntity.BodyBuilder resp = ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, removeMfaCookie.toString());

        if (dto.accessToken() != null) {
            ResponseCookie accessCookie = CookieUtils.createAccessTokenCookie(dto.accessToken());
            ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(dto.refreshToken());
            resp.header(HttpHeaders.SET_COOKIE, accessCookie.toString());
            resp.header(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        return resp.body(dto);
    }


    @RateLimiter(name = "verifyTotp", key = "#jwt.subject")
    @Operation(
        summary     = "Verify TOTP during login",
        description = "Validate TOTP code as part of the login flow",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login TOTP verified",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = VerifyTotpResultDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid TOTP code",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/verify-totp",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EntityModel<VerifyTotpResultDTO>> verifyTotp(
        @AuthenticationPrincipal Jwt jwt,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "TOTP verification payload",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = VerifyTotpRequestDTO.class)
            )
        )
        @RequestBody @Valid VerifyTotpRequestDTO request
    ) {
        log.debug("REST request to verify TOTP for user : {}", jwt.getSubject());
        var verifyTotpResult = authenticationService.verifyTotp(jwt.getSubject(), request.code());

        ResponseCookie accessCookie = CookieUtils.createAccessTokenCookie(verifyTotpResult.accessToken());
        ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(verifyTotpResult.refreshToken());
        ResponseCookie removeMfaCookie = CookieUtils.removeMfaTokenCookie();

        ResponseEntity.BodyBuilder resp = ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .header(HttpHeaders.SET_COOKIE, removeMfaCookie.toString());

        EntityModel<VerifyTotpResultDTO> model = EntityModel.of(verifyTotpResult);
        model.add(linkTo(methodOn(AuthenticationController.class).verifyTotp(jwt, request)).withSelfRel());
        model.add(linkTo(methodOn(AuthenticationController.class).refreshToken(null)).withRel("refresh-token"));
        model.add(linkTo(methodOn(AuthenticationController.class).logout(null)).withRel("logout"));

        return resp.body(model);
    }

    @Operation(
        summary     = "Log out user",
        description = "Invalidate the current access token",
        responses = {
            @ApiResponse(responseCode = "200", description = "Logout successful",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE
                )
            )
        }
    )
    @PostMapping(
        path     = "/logout",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> logout(
        @AuthenticationPrincipal Jwt jwt) {
        log.debug("REST request to logout user : {}", jwt.getSubject());
        authenticationService.logout(jwt.getSubject());

        ResponseCookie accessCookie  = CookieUtils.removeAccessTokenCookie();
        ResponseCookie refreshCookie = CookieUtils.removeRefreshTokenCookie();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
            .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
            .build();
    }

    @RateLimiter(name = "verifyEmail", key = "#token")
    @Operation(
        summary     = "Verify email address",
        description = "Confirm email using a verification token",
        responses = {
            @ApiResponse(responseCode = "200", description = "Email verified",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = EmailVerificationDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            ),
            @ApiResponse(responseCode = "409", description = "Conflict - email already verified",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    @SecurityRequirements
    @GetMapping(
        path     = "/verify-email",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmailVerificationDTO> verifyEmail(
        @Parameter(
            description = "The verification token sent to the user's email",
            example     = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        @RequestParam @NotBlank(message = "Field is required.") String token) {

        log.debug("REST request to verify email with token : {}", token);
        EmailVerificationDTO dto = authenticationService.verifyEmail(token);

        if (dto.mfaEnabled()) {
            ResponseCookie cookie = CookieUtils.createMfaTokenCookie(dto.mfaToken());
            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(dto);
        }

        return ResponseEntity.ok(dto);
    }

    @RateLimiter(name = "forgotPassword", key = "#request.email")
    @Operation(
        summary     = "Forgot password",
        description = "Send a password reset email",
        responses = {
            @ApiResponse(responseCode = "200", description = "Reset email queued",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ForgotPasswordDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class))
            )
        }
    )
    @SecurityRequirements
    @PostMapping(
        path     = "/forgot-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ForgotPasswordDTO> forgotPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload containing the email to reset",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = ForgotPasswordRequestDTO.class)
            )
        )
        @RequestBody @Valid ForgotPasswordRequestDTO request
    ) {
        log.debug("REST request to forgot password for email : {}", request.email());
        ForgotPasswordDTO response = authenticationService.forgotPassword(request.email());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary     = "Reset password",
        description = "Complete password reset with token and new password",
        responses = {
            @ApiResponse(responseCode = "200", description = "Password reset successful",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ResetPasswordDTO.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired reset token",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @SecurityRequirements
    @PostMapping(
        path     = "/reset-password",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResetPasswordDTO> resetPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Password reset payload",
            required    = true,
            content     = @Content(
                schema = @Schema(implementation = ResetPasswordRequestDTO.class)
            )
        )
        @RequestBody @Valid ResetPasswordRequestDTO dto
    ) {
        log.debug("REST request to reset password : {}", dto.token());
        ResetPasswordDTO response = authenticationService.resetPassword(dto);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }
}
