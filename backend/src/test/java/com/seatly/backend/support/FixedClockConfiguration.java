package com.seatly.backend.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

// Frozen "now" so date rules in tests don't rot as the real date moves on.
@TestConfiguration(proxyBeanMethods = false)
public class FixedClockConfiguration {

    public static final ZoneId ZONE = ZoneOffset.UTC;
    public static final Instant FIXED_INSTANT = Instant.parse("2026-09-29T09:00:00Z");
    public static final LocalDateTime NOW = LocalDateTime.ofInstant(FIXED_INSTANT, ZONE);

    // @Primary overrides ClockConfig's Clock without renaming or disabling it.
    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(FIXED_INSTANT, ZONE);
    }
}
