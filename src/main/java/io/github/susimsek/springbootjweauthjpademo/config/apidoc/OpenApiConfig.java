package io.github.susimsek.springbootjweauthjpademo.config.apidoc;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class OpenApiConfig {

    private final ApplicationProperties props;

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
}
