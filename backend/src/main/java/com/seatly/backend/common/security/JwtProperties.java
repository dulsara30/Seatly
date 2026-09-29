package com.seatly.backend.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * signingKey is Base64 and comes from the JWT_SIGNING_KEY environment
 * variable — application.yml only holds the placeholder, never a value. If
 * the variable is missing the app refuses to start rather than running with
 * a guessable key.
 */
@ConfigurationProperties(prefix = "seatly.jwt")
public record JwtProperties(String signingKey, Duration accessTokenTtl) {
}
