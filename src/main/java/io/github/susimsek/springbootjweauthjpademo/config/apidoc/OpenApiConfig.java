package io.github.susimsek.springbootjweauthjpademo.config.apidoc;

import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiter;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemDetail;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class OpenApiConfig {

    private final ApplicationProperties props;

    @PostConstruct
    public void customizeProblemDetail() {
        SpringDocUtils.getConfig()
            .replaceWithClass(
                org.springframework.http.ProblemDetail.class,
                ProblemDetail.class
            );
    }

    @Bean
    public OpenAPI openAPI() {
        ApplicationProperties.ApiDocs docs = props.getApiDocs();
        // Define the HTTP Bearer auth scheme
        SecurityScheme bearerScheme = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization")
            .description(
                "JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\"");

        // Build the OpenAPI definition
        OpenAPI openAPI = new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearerAuth", bearerScheme)
            )
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .info(new Info()
                .title(docs.getTitle())
                .description(docs.getDescription())
                .version(docs.getVersion())
                .termsOfService(docs.getTermsOfServiceUrl())
                .contact(new Contact()
                    .name(docs.getContactName())
                    .url(docs.getContactUrl())
                    .email(docs.getContactEmail()))
                .license(new License()
                    .name(docs.getLicense())
                    .url(docs.getLicenseUrl()))
            );

        for (ApplicationProperties.ApiDocs.Server server : docs.getServers()) {
            openAPI.addServersItem(
                new Server()
                    .url(server.getUrl())
                    .description(server.getDescription())
            );
        }
        return openAPI;
    }


    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            addSecurityErrorResponse(operation, handlerMethod);
            addRateLimitErrorResponse(operation, handlerMethod);
            addDefaultErrorResponse(operation);
            return operation;
        };
    }

    private void addDefaultErrorResponse(Operation operation) {
        operation.getResponses().addApiResponse("500",
            new io.swagger.v3.oas.models.responses.ApiResponse()
                .description("Internal Server Error")
                .content(new Content()
                    .addMediaType(MediaType.APPLICATION_JSON_VALUE,
                        new io.swagger.v3.oas.models.media.MediaType()
                            .schema(new Schema<ProblemDetail>().$ref(
                                "#/components/schemas/ProblemDetail"))
                            .example(getDefaultErrorExample()))));
    }

    private void addRateLimitErrorResponse(Operation operation, HandlerMethod handlerMethod) {
        if (handlerMethod.getMethod().isAnnotationPresent(RateLimiter.class)) {
            operation.getResponses().addApiResponse("429",
                new io.swagger.v3.oas.models.responses.ApiResponse()
                    .description("Too Many Requests")
                    .content(new Content()
                        .addMediaType(MediaType.APPLICATION_JSON_VALUE,
                            new io.swagger.v3.oas.models.media.MediaType()
                                .schema(new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail"))
                                .example(getRateLimitErrorExample()))));
        }
    }

    private void addSecurityErrorResponse(Operation operation, HandlerMethod handlerMethod) {
        SecurityRequirements sr = handlerMethod.getMethod().getAnnotation(SecurityRequirements.class);
        boolean hasSecurity = (sr == null || sr.value().length > 0);
        if (hasSecurity) {
            operation.getResponses().addApiResponse("401",
                new ApiResponse().description("Unauthorized")
                  .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE,
                      new io.swagger.v3.oas.models.media.MediaType()
                         .schema(new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail"))
                         .example(getUnauthorizedExample()))));
            operation.getResponses().addApiResponse("403",
                new ApiResponse().description("Access Denied")
                  .content(new Content().addMediaType(MediaType.APPLICATION_JSON_VALUE,
                      new io.swagger.v3.oas.models.media.MediaType()
                         .schema(new Schema<ProblemDetail>().$ref("#/components/schemas/ProblemDetail"))
                         .example(getAccessDeniedExample()))));
        }
    }

    private Map<String, Object> getDefaultErrorExample() {
        ProblemType type = ProblemType.SERVER_ERROR;
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("type", type.getType());
        example.put("title", "Internal Server Error");
        example.put("status", 500);
        example.put("detail", "An unexpected error occurred. Please try again later.");
        example.put("instance", "/api/qrcode");
        example.put("error", type.getError());
        return example;
    }

    private Map<String, Object> getRateLimitErrorExample() {
        ProblemType type = ProblemType.RATE_LIMIT_EXCEEDED;
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("type", type.getType());
        example.put("title", "Rate Limit Exceeded");
        example.put("status", 429);
        example.put("detail", "You have exceeded the allowed request rate.");
        example.put("instance", "/api/qrcode");
        example.put("error", type.getError());
        return example;
    }

    private Map<String, Object> getUnauthorizedExample() {
        ProblemType type = ProblemType.INVALID_TOKEN;
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("type", type.getType());
        example.put("title", "Invalid Token");
        example.put("status", 401);
        example.put("detail", "Invalid token.");
        example.put("instance", "/api/qrcode");
        example.put("error", type.getError());
        return example;
    }

    private Map<String, Object> getAccessDeniedExample() {
        ProblemType type = ProblemType.ACCESS_DENIED;
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("type", type.getType());
        example.put("title", "Access Denied");
        example.put("status", 403);
        example.put("detail", "You do not have permission to access this resource.");
        example.put("instance", "/api/qrcode");
        example.put("error", type.getError());
        return example;
    }
}
