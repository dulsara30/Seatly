package com.seatly.backend.rsvp;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class RsvpConcurrencyIntegrationTest extends AbstractIntegrationTest {

    // A race can be won by luck once; repeating it makes a missing lock fail reliably.
    private static final int ROUNDS = 20;
    private static final int CROWD_SIZE = 8;
    private static final long TIMEOUT_SECONDS = 30;

    @Test
    void twoPeopleRacingForTheLastSeatNeverBothGetIt() throws Exception {
        for (int round = 0; round < ROUNDS; round++) {
            long eventId = eventWithSeatLimit(2);
            testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);

            List<MvcTestResult> results = rsvpSimultaneously(eventId, List.of(testData.insertUser(), testData.insertUser()));

            assertThat(results).allSatisfy(result -> assertThat(result).hasStatus(HttpStatus.CREATED));
            assertThat(statusesOf(results)).as("round %d", round)
                    .containsExactlyInAnyOrder(RsvpStatus.CONFIRMED.name(), RsvpStatus.WAITLISTED.name());
            assertThat(testData.countRsvps(eventId, RsvpStatus.CONFIRMED)).as("round %d: confirmed", round).isEqualTo(2);
            assertThat(testData.waitlistPositions(eventId).values()).as("round %d: queue", round).containsExactly(1);
        }
    }

    @Test
    void crowdRacingForOneSeatGetsOneWinnerAndAnOrderlyQueue() throws Exception {
        long eventId = eventWithSeatLimit(1);
        List<Long> crowd = IntStream.range(0, CROWD_SIZE).mapToObj(i -> testData.insertUser()).toList();

        List<MvcTestResult> results = rsvpSimultaneously(eventId, crowd);

        assertThat(results).allSatisfy(result -> assertThat(result).hasStatus(HttpStatus.CREATED));
        assertThat(testData.countRsvps(eventId, RsvpStatus.CONFIRMED)).isEqualTo(1);
        assertThat(testData.waitlistPositions(eventId).values())
                .containsExactlyElementsOf(IntStream.rangeClosed(1, CROWD_SIZE - 1).boxed().toList());
    }

    private long eventWithSeatLimit(int seatLimit) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Last Seat", EventMode.PHYSICAL,
                EventStatus.UPCOMING, seatLimit, NOW.plusWeeks(1));
    }

    // The barrier releases every thread at once so the requests genuinely overlap.
    private List<MvcTestResult> rsvpSimultaneously(long eventId, List<Long> userIds) throws Exception {
        List<String> tokens = userIds.stream().map(this::bearerTokenFor).toList();
        CyclicBarrier startingLine = new CyclicBarrier(tokens.size());

        try (ExecutorService threads = Executors.newFixedThreadPool(tokens.size())) {
            List<Future<MvcTestResult>> pending = new ArrayList<>();
            for (String token : tokens) {
                pending.add(threads.submit(() -> {
                    startingLine.await();
                    return mvc.post().uri(RSVP_PATH, eventId).header(HttpHeaders.AUTHORIZATION, token).exchange();
                }));
            }
            List<MvcTestResult> results = new ArrayList<>();
            for (Future<MvcTestResult> result : pending) {
                results.add(result.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            }
            return results;
        }
    }

    private List<String> statusesOf(List<MvcTestResult> results) {
        return results.stream().<String>map(result -> readJson(result, "$.results[0].status")).toList();
    }
}
