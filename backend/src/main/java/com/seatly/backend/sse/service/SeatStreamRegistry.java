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

/**
 * Every open seat stream, grouped by event.
 *
 * Thread safety: streams are opened on request threads, closed from Tomcat's
 * completion/timeout/error callbacks, and written to from whichever thread
 * committed a seat change — all concurrently. The map is a ConcurrentHashMap
 * of concurrent sets, and adding or removing an emitter happens inside
 * compute(): otherwise a stream could be added to an event's set in the same
 * instant the last other stream removed that set from the map, and it would
 * never receive an update.
 *
 * Leaks: an emitter is removed on completion, timeout and error — the three
 * ways a stream ends — and a send that fails removes it too. The heartbeat
 * ensures a vanished client is noticed within one interval.
 *
 * KNOWN LIMIT — one instance only. This registry lives in this JVM's memory.
 * Run two instances behind a load balancer and a viewer connected to
 * instance A never hears a change committed on instance B: B publishes to its
 * own registry, which doesn't hold that viewer's stream. The fix is Redis
 * pub/sub fan-out: every instance publishes the committed change to a Redis
 * channel, and every instance subscribes and broadcasts to its own local
 * emitters. Because services already publish through a Spring
 * ApplicationEvent, that's a change to SeatStreamListener alone — the
 * services, this registry and the endpoint stay as they are.
 */
@Component
public class SeatStreamRegistry {

    /** EventSource reconnects by itself, so a bounded stream just re-opens — and can't linger forever. */
    static final Duration STREAM_TIMEOUT = Duration.ofMinutes(30);

    private static final String SEAT_UPDATE_EVENT = "seat-update";
    private static final String HEARTBEAT_COMMENT = "ping";

    private final Map<Long, Set<SseEmitter>> emittersByEventId = new ConcurrentHashMap<>();

    /**
     * Opens a stream and sends the current counts straight away. That first
     * message matters on reconnect: any update missed while disconnected is
     * superseded by the truth, instead of the page keeping a stale count.
     */
    public SseEmitter open(SeatCountDto current) {
        Long eventId = current.eventId();
        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT.toMillis());
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

    /**
     * An SSE comment line (": ping"). Browsers ignore it; load balancers and
     * proxies see traffic and keep the idle connection open. A failed write
     * here is also how a silently-dropped client gets cleaned up.
     */
    public void sendHeartbeat() {
        emittersByEventId.forEach((eventId, emitters) -> emitters.forEach(
                emitter -> send(eventId, emitter, SseEmitter.event().comment(HEARTBEAT_COMMENT))));
    }

    /** Open streams for one event — for tests and diagnostics. */
    public int openStreamCount(Long eventId) {
        return emittersByEventId.getOrDefault(eventId, Set.of()).size();
    }

    private void sendSeatUpdate(Long eventId, SseEmitter emitter, SeatCountDto seatCount) {
        send(eventId, emitter, SseEmitter.event().name(SEAT_UPDATE_EVENT).data(seatCount, MediaType.APPLICATION_JSON));
    }

    // A write fails when the client has gone (IOException) or the emitter has
    // already completed (IllegalStateException). Either way the stream is dead.
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
