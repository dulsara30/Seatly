package com.seatly.backend.sse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Load balancers and reverse proxies close connections that have been idle
 * for about 60s (AWS ALB's default; Render and Railway are similar). A quiet
 * event — nobody RSVPing — would otherwise have its viewers cut off once a
 * minute. Every 25s stays comfortably inside that window.
 */
@Component
@RequiredArgsConstructor
public class SeatStreamHeartbeat {

    static final long HEARTBEAT_INTERVAL_MS = 25_000;

    private final SeatStreamRegistry registry;

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void beat() {
        registry.sendHeartbeat();
    }
}
