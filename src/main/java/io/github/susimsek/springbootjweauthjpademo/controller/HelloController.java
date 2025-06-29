package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.GreetRequestDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.susimsek.springbootjweauthjpademo.security.SecurityUtils.AUTHORITIES_KEY;

@RestController
@RequestMapping("/api/hello")
@RequiredArgsConstructor
@Tag(name = "hello", description = "Endpoints for simple greetings")
@Slf4j
public class HelloController {

    @Operation(
        summary = "Get a friendly greeting for the authenticated user",
        description = "Returns a message greeting the authenticated user and listing their roles.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "Greeting returned",
                content      = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
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
            )
        }
    )
    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String helloAll(@AuthenticationPrincipal Jwt jwt) {
        String user = jwt.getSubject();
        log.debug("REST request to helloAll for user : {}", user);
        var roles = jwt.getClaimAsStringList(AUTHORITIES_KEY);
        return "Hello, " + user + "! Your roles: " + roles;
    }

    @Operation(
        summary = "Get an admin-specific greeting",
        description = "Returns a special greeting for users with the ADMIN role.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "Admin greeting returned",
                content      = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
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
            )
        }
    )
    @GetMapping(path     = "/admin", produces = MediaType.TEXT_PLAIN_VALUE)
    public String helloAdmin(@AuthenticationPrincipal Jwt jwt) {
        log.debug("REST request to helloAdmin for user : {}", jwt.getSubject());
        return "Hello Admin, " + jwt.getSubject() + "!";
    }

    @RateLimiter(name = "greet", key = "#jwt.subject")
    @Operation(
        summary     = "Echo a custom message",
        description = "Accepts a JSON payload with a message and echoes it back, including the user's name.",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description  = "Message echoed back",
                content      = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
            ),
            @ApiResponse(
                responseCode = "400",
                description  = "Bad request",
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
            @ApiResponse(
                responseCode = "403",
                description  = "Forbidden",
                content      = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema    = @Schema(implementation = ProblemDetail.class)
                )
            )
        }
    )
    @PostMapping(
        path     = "/greet",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String greet(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Payload containing the message to echo back",
            required = true,
            content = @Content(
                schema = @Schema(implementation = GreetRequestDTO.class)
            )
        )
        @RequestBody @Valid GreetRequestDTO request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        log.debug("REST request to greet user : {}, message : {}", jwt.getSubject(), request.message());
        return "Hello " + jwt.getSubject() + ", you said: " + request.message();
    }
}
