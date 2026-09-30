package com.seatly.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    private static final String JWT_FORMAT = "JWT";
    private static final String BEARER = "bearer";

    // Global so Swagger also sends the token on public GETs (meetingLink needs the organiser's token).
    @Bean
    public OpenAPI seatlyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Seatly API")
                        .version("v1")
                        .description("Event RSVP & waitlist manager — organisers create events with a seat "
                                + "limit, attendees RSVP and are confirmed or waitlisted."))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER)
                        .bearerFormat(JWT_FORMAT)))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
