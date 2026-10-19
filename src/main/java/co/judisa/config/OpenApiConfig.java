package co.judisa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger UI en /swagger-ui.html con autenticación Bearer. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI judisaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Judisa")
                        .version("v1")
                        .description("API REST del Taller Satélite Judisa Jean: empleados, referencias, cortes y pagos. "
                                + "Inicie sesión con POST /api/v1/auth/login y use el accessToken en el botón Authorize."))
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
