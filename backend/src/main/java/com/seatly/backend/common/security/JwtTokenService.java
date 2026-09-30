package com.seatly.backend.common.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private static final String ISSUER = "seatly";

    private final SecretKey signingKey;
    private final JwtProperties properties;
    private final Clock clock;
    private final JwtParser parser;

    public JwtTokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.signingKey()));
        this.parser = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(ISSUER)
                .clock(() -> Date.from(clock.instant()))
                .build();
    }

    public String issueAccessToken(Long userId) {
        Instant issuedAt = clock.instant();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(properties.accessTokenTtl())))
                .signWith(signingKey)
                .compact();
    }

    public Optional<Long> parseUserId(String token) {
        try {
            return Optional.of(Long.valueOf(parser.parseSignedClaims(token).getPayload().getSubject()));
        } catch (JwtException | IllegalArgumentException invalidToken) {
            return Optional.empty();
        }
    }
}
