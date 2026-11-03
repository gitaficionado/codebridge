
package com.codebridge.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "CodeBridge Backend API",
                version = "1.0.0",
                description = """
                        REST API for analyzing potential integration
                        conflicts between public GitHub repositories.

                        Maven dependency severity levels describe
                        version differences, not confirmed
                        incompatibilities or vulnerabilities.
                        """
        ),
        security = @SecurityRequirement(name = "basicAuth")
)
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "HTTP Basic authentication for CodeBridge APIs"
)
public class OpenApiConfig {
}
