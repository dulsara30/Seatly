package com.seatly.backend.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Freezes "now" for every test. Without this, a test asserting that
 * 2026-10-15 is "in the future" silently starts failing on 2026-10-16 —
 * this is what the Clock bean in ClockConfig exists for.
 */
@TestConfiguration(proxyBeanMethods = false)
public class FixedClockConfiguration {

    public static final ZoneId ZONE = ZoneOffset.UTC;
    public static final Instant FIXED_INSTANT = Instant.parse("2026-09-29T09:00:00Z");
    public static final LocalDateTime NOW = LocalDateTime.ofInstant(FIXED_INSTANT, ZONE);

    // @Primary wins over ClockConfig's bean without renaming or disabling it.
    @Bean
    @Primary
    Clock fixedClock() {
        return Clock.fixed(FIXED_INSTANT, ZONE);
    }
}
