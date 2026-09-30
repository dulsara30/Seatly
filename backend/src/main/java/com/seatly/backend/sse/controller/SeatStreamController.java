package com.seatly.backend.sse.controller;

import com.seatly.backend.common.constant.ApiPaths;
import com.seatly.backend.common.constant.ApiResponseCodes;
import com.seatly.backend.sse.service.SeatStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Tag(name = "Live seats", description = "Server-Sent Events: seat counts as they change")
public class SeatStreamController {

    // Nginx buffers proxied responses by default, which holds SSE messages
    // back until the buffer fills — so the events would simply never arrive.
    private static final String X_ACCEL_BUFFERING = "X-Accel-Buffering";
    private static final String NO_BUFFERING = "no";

    private final SeatStreamService seatStreamService;

    /**
     * Public, like the event itself: it carries only counts, no personal data.
     * no-transform stops gzip (in Next, or a CDN) from buffering the stream.
     */
    @GetMapping(value = ApiPaths.EVENT_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream seat counts",
            description = "Sends the current counts immediately, then an event: seat-update on every change. "
                    + "A comment line every 25s keeps the connection alive through load balancers.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "A text/event-stream of seat-update events")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event, or it was deleted")
    public ResponseEntity<SseEmitter> streamSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().noTransform())
                .header(X_ACCEL_BUFFERING, NO_BUFFERING)
                .body(seatStreamService.openSeatStream(eventId));
    }
}
