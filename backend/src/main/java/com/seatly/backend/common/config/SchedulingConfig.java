package com.seatly.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Turns on @Scheduled — today the SSE heartbeat. When the outbox sender and
 * reminder jobs arrive they run here too; with more than one instance, each
 * of those will need ShedLock so they don't fire once per instance.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
