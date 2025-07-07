package io.github.susimsek.springbootjweauthjpademo.controller;

import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiter;
import io.github.susimsek.springbootjweauthjpademo.dto.response.QrCodeDTO;
import io.github.susimsek.springbootjweauthjpademo.config.resolver.QrCodeSize;
import io.github.susimsek.springbootjweauthjpademo.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
@Validated
@Tag(
    name = "qrcode",
    description = "Endpoints for generating QR code images"
)
@Slf4j
public class QrCodeController {

    private final QrCodeService qrCodeService;

    @RateLimiter(name = "qrCode", key = "#ip")
    @Operation(
        summary     = "Generate QR Code",
        description = "Generates a PNG-format QR code for the given data and size.",
        responses = {
            @ApiResponse(responseCode = "200", description = "PNG image returned",
                content = @Content(mediaType = "image/png")),
            @ApiResponse(
                responseCode = "304",
                description  = "Not modified",
                content      = @Content
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
    @GetMapping(produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(
        @Parameter(
            description = "The text or URL to encode into the QR code",
            required    = true,
            in          = ParameterIn.QUERY,
            example     = "https://example.com"
        )
        @NotBlank(message = "Field is required.")
        @RequestParam("data")
        String data,

        QrCodeSize qrCodeSize,
        HttpServletRequest request
    ) {
        log.debug("REST request to generate QR code: data='{}', size={}", data, qrCodeSize);
        QrCodeDTO dto = qrCodeService.generateQrCode(data, qrCodeSize);
        String etag = "\"" + dto.contentHash() + "\"";

        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .build();
        }

        return ResponseEntity.ok()
            .eTag(etag)
            .contentType(MediaType.IMAGE_PNG)
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
            .body(dto.content());
    }
}
