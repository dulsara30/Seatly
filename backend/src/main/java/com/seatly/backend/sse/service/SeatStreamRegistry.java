package com.seatly.backend.sse.service;

import com.seatly.backend.event.payload.SeatCountDto;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// KNOWN LIMIT: in-memory, so one instance only; multi-instance needs Redis pub/sub fan-out.
@Component
public class SeatStreamRegistry {

    // EventSource reconnects by itself, so a bounded stream just re-opens instead of lingering.
    static final Duration STREAM_TIMEOUT = Duration.ofMinutes(30);

    private static final String SEAT_UPDATE_EVENT = "seat-update";
    private static final String HEARTBEAT_COMMENT = "ping";

    private final Map<Long, Set<SseEmitter>> emittersByEventId = new ConcurrentHashMap<>();

    // The first snapshot heals any update missed while the client was disconnected.
    public SseEmitter open(SeatCountDto current) {
        Long eventId = current.eventId();
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
        // Inside compute(), so an emitter can't join a set that remove() is dropping from the map.
        emittersByEventId.compute(eventId, (id, emitters) -> {
            Set<SseEmitter> target = emitters == null ? ConcurrentHashMap.newKeySet() : emitters;
            target.add(emitter);
            return target;
        });
        emitter.onCompletion(() -> remove(eventId, emitter));
        emitter.onTimeout(() -> remove(eventId, emitter));
        emitter.onError(error -> remove(eventId, emitter));

        sendSeatUpdate(eventId, emitter, current);
        return emitter;
    }

    public void broadcast(SeatCountDto seatCount) {
        emittersByEventId.getOrDefault(seatCount.eventId(), Set.of())
                .forEach(emitter -> sendSeatUpdate(seatCount.eventId(), emitter, seatCount));
    }

    // Browsers ignore SSE comments; proxies see traffic, and a failed write drops a dead client.
    public void sendHeartbeat() {
        emittersByEventId.forEach((eventId, emitters) -> emitters.forEach(
                emitter -> send(eventId, emitter, SseEmitter.event().comment(HEARTBEAT_COMMENT))));
    }

    // For tests and diagnostics.
    public int openStreamCount(Long eventId) {
        return emittersByEventId.getOrDefault(eventId, Set.of()).size();
    }

    private void sendSeatUpdate(Long eventId, SseEmitter emitter, SeatCountDto seatCount) {
        send(eventId, emitter, SseEmitter.event().name(SEAT_UPDATE_EVENT).data(seatCount, MediaType.APPLICATION_JSON));
    }

    // IOException (client gone) or IllegalStateException (already completed): the stream is dead.
    private void send(Long eventId, SseEmitter emitter, SseEmitter.SseEventBuilder message) {
        try {
            emitter.send(message);
        } catch (IOException | IllegalStateException deadStream) {
            remove(eventId, emitter);
        }
    }

    private void remove(Long eventId, SseEmitter emitter) {
        emittersByEventId.computeIfPresent(eventId, (id, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
