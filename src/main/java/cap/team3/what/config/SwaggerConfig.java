package cap.team3.what.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@OpenAPIDefinition(
        servers = {
            @Server(url = "https://capstonepractice.site", description = "ec2 서버"),
            @Server(url = "http://localhost:8080", description = "로컬 서버")
        })
@Configuration
public class SwaggerConfig {
    private static final String SCHEME_NAME = "BearerAuth";

    SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

    SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SCHEME_NAME);

    @Bean
    protected OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(SCHEME_NAME, securityScheme))
                .info(apiInfo())
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("WHAT")
                .description("WHAT API 명세")
                .version("1.0.0");
    }
}