package com.iliamalafeev.bookstore.bookstore_backend.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(name = "Ilia Malafeev", email = "iliamalafeev1@gmail.com", url = "https://github.com/IliaMalafeev"),
                description = "OpenAPI documentation for Book Store Project backend application.",
                title = "OpenAPI specification - Ilia Malafeev",
                version = "1.0",
                license = @License(name = "Licence name and url here", url = "https://license-url-here.com"),
                termsOfService = "https://terms-url-here.com"
        ),
        servers = {
                @Server(description = "Local environment", url = "http://localhost:8080"),
                @Server(description = "Production environment", url = "https://production-url-here.com"),
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = "To access secure endpoints, please provide a valid JWT authorisation token. " +
                "You can claim such token via \"/api/auth/authenticate\" endpoint of Authentication Controller below.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfiguration {
}
