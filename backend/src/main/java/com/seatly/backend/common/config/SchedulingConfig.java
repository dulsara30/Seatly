package com.seatly.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

// Future jobs here will need ShedLock once there is more than one instance.
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
