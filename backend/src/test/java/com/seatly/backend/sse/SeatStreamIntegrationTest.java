package com.seatly.backend.sse;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import com.seatly.backend.event.payload.SeatCountDto;
import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.json.JsonMapper;

// Plain MockMvc: MockMvcTester waits for async requests to finish, and a stream never does.
class SeatStreamIntegrationTest extends AbstractIntegrationTest {

    private static final String STREAM_PATH = "/v1/events/{eventId}/stream";
    private static final String SEAT_UPDATE_EVENT_LINE = "event:seat-update";
    private static final String DATA_PREFIX = "data:";
    // Generous for slow CI; a passing test returns as soon as its push arrives.
    private static final Duration PUSH_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration ACTION_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration POLL_INTERVAL = Duration.ofMillis(50);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void sendsCurrentCountsAsSoonAsTheStreamOpens() throws Exception {
        long eventId = eventWithSeatLimit(5);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 2);

        MvcResult stream = openStream(eventId);

        awaitPush(stream, new SeatCountDto(eventId, 3, 2));
        assertThat(stream.getResponse().getContentType()).startsWith(MediaType.TEXT_EVENT_STREAM_VALUE);
        assertThat(stream.getResponse().getHeader("X-Accel-Buffering")).isEqualTo("no");
    }

    @Test
    void pushesTheNewCountWhenSomeoneElseRsvps() throws Exception {
        long eventId = eventWithSeatLimit(2);
        MvcResult stream = openStream(eventId);
        awaitPush(stream, new SeatCountDto(eventId, 2, 0));

        inAnotherThread(() -> rsvpAs(eventId, testData.insertUser()));

        awaitPush(stream, new SeatCountDto(eventId, 1, 1));
    }

    @Test
    void pushesEvenWhenTheRsvpIsWaitlisted() throws Exception {
        long eventId = eventWithSeatLimit(1);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);
        MvcResult stream = openStream(eventId);

        inAnotherThread(() -> rsvpAs(eventId, testData.insertUser()));

        awaitSeatUpdateCount(stream, 2);
    }

    @Test
    void pushesWhenAnRsvpIsCancelled() throws Exception {
        long eventId = eventWithSeatLimit(3);
        long attendee = testData.insertUser();
        testData.insertRsvp(eventId, attendee, RsvpStatus.CONFIRMED);
        MvcResult stream = openStream(eventId);
        awaitPush(stream, new SeatCountDto(eventId, 2, 1));

        inAnotherThread(() -> cancelRsvpAs(eventId, attendee));

        awaitPush(stream, new SeatCountDto(eventId, 3, 0));
    }

    @Test
    void pushesWhenARaisedSeatLimitPromotesTheWaitlist() throws Exception {
        long eventId = eventWithSeatLimit(1);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);
        testData.insertWaitlist(eventId, 1);
        MvcResult stream = openStream(eventId);
        awaitPush(stream, new SeatCountDto(eventId, 0, 1));

        inAnotherThread(() -> patch(eventId, new UpdateEventRequestDto(null, null, null, null, null, null, 3, null)));

        awaitPush(stream, new SeatCountDto(eventId, 1, 2));
    }

    @Test
    void refusesToStreamAnUnknownEvent() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(STREAM_PATH, Long.MAX_VALUE)).andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getRequest().isAsyncStarted()).isFalse();
    }

    // No Authorization header: the stream is public.
    private MvcResult openStream(long eventId) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(STREAM_PATH, eventId).header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(request().asyncStarted())
                .andReturn();
    }

    private long eventWithSeatLimit(int seatLimit) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Live Event", EventMode.PHYSICAL,
                EventStatus.UPCOMING, seatLimit, NOW.plusWeeks(1));
    }

    // Acts as a second browser: its own request thread and transaction.
    private void inAnotherThread(Runnable action) throws Exception {
        try (ExecutorService thread = Executors.newSingleThreadExecutor()) {
            thread.submit(action).get(ACTION_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        }
    }

    private void awaitPush(MvcResult stream, SeatCountDto expected) throws InterruptedException {
        String expectedData = DATA_PREFIX + jsonMapper.writeValueAsString(expected);
        awaitUntil(stream, () -> body(stream).contains(expectedData),
                "a seat-update carrying " + expectedData);
    }

    private void awaitSeatUpdateCount(MvcResult stream, int count) throws InterruptedException {
        awaitUntil(stream, () -> body(stream).split(SEAT_UPDATE_EVENT_LINE, -1).length - 1 >= count,
                count + " seat-update events");
    }

    private void awaitUntil(MvcResult stream, BooleanSupplier condition, String description)
            throws InterruptedException {
        Instant deadline = Instant.now().plus(PUSH_TIMEOUT);
        while (!condition.getAsBoolean()) {
            if (Instant.now().isAfter(deadline)) {
                throw new AssertionError("Stream never received " + description + ". It contained:\n" + body(stream));
            }
            Thread.sleep(POLL_INTERVAL);
        }
    }

    private String body(MvcResult stream) {
        return new String(stream.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }
}
