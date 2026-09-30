package com.seatly.backend.sse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatStreamHeartbeat {

    // 25s stays inside the ~60s idle timeout of load balancers and proxies (e.g. AWS ALB).
    static final long HEARTBEAT_INTERVAL_MS = 25_000;

    private final SeatStreamRegistry registry;

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL_MS)
    public void beat() {
        registry.sendHeartbeat();
    }
}
