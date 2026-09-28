package com.seatly.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI seatlyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Seatly API")
                        .version("v1")
                        .description("Event RSVP & waitlist manager — organisers create events with a seat "
                                + "limit, attendees RSVP and are confirmed or waitlisted."));
    }
}
