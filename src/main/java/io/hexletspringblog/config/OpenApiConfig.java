package io.hexletspringblog.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_JWT = "bearer-jwt";

    @Bean
    public OpenAPI hexletBlogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hexlet Spring Blog API")
                        .version("1.0")
                        .description("""
                                REST API блога: посты, теги, пользователи, комментарии.
                                Защищённые операции требуют JWT в заголовке `Authorization: Bearer <token>`.
                                Токен выдаётся методом `POST /api/login` (логин/пароль в теле запроса).
                                """))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT))
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT,
                                new SecurityScheme()
                                        .name(BEARER_JWT)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT из ответа POST /api/login")));
    }
}
