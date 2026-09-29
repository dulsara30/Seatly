package com.seatly.backend.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * "Now" is a dependency, not a static call. Services read the time through
 * this bean, so a test can pass Clock.fixed(...) and make time-based rules
 * like "the event date must be in the future" deterministic.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
