package com.seatly.backend.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "seatly.jwt")
public record JwtProperties(String signingKey, Duration accessTokenTtl) {
}
