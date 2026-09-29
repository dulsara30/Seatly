package com.seatly.backend.rsvp;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Every way a seat or a place in the queue can open up, and that each one
 * leaves the queue promoted and renumbered the same way — they all go
 * through WaitlistManager.rebalance.
 */
class WaitlistPromotionIntegrationTest extends AbstractIntegrationTest {

    private static final int SEAT_LIMIT = 2;

    @Test
    void cancellingAConfirmedSeatPromotesFirstInLineAndClosesTheQueue() {
        long eventId = event(EventMode.PHYSICAL);
        long leaver = testData.insertUser();
        testData.insertRsvp(eventId, leaver, RsvpStatus.CONFIRMED);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);
        List<Long> queue = testData.insertWaitlist(eventId, 3);

        cancelRsvpAs(eventId, leaver);

        assertThat(testData.rsvpStatusOf(eventId, queue.get(0))).isEqualTo(RsvpStatus.CONFIRMED.name());
        assertThat(testData.countRsvps(eventId, RsvpStatus.CONFIRMED)).isEqualTo(SEAT_LIMIT);
        assertThat(testData.waitlistPositions(eventId)).containsExactly(
                Map.entry(queue.get(1), 1), Map.entry(queue.get(2), 2));
    }

    // Leaving the queue frees no seat — nobody is promoted, everyone behind moves up.
    @Test
    void leavingTheWaitlistMovesEveryoneBehindUpOne() {
        long eventId = event(EventMode.PHYSICAL);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, SEAT_LIMIT);
        List<Long> queue = testData.insertWaitlist(eventId, 3);

        cancelRsvpAs(eventId, queue.get(1));

        assertThat(testData.countRsvps(eventId, RsvpStatus.CONFIRMED)).isEqualTo(SEAT_LIMIT);
        assertThat(testData.waitlistPositions(eventId)).containsExactly(
                Map.entry(queue.get(0), 1), Map.entry(queue.get(2), 2));
    }

    // The part deferred in Layer 5: raising the limit now fills the new seats.
    @Test
    void raisingTheSeatLimitPromotesFromTheFrontOfTheQueue() {
        long eventId = event(EventMode.PHYSICAL);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, SEAT_LIMIT);
        List<Long> queue = testData.insertWaitlist(eventId, 3);

        MvcTestResult result = patch(eventId, seatLimit(SEAT_LIMIT + 2));

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].availableSeats").isEqualTo(0);
        assertThat(testData.rsvpStatusOf(eventId, queue.get(0))).isEqualTo(RsvpStatus.CONFIRMED.name());
        assertThat(testData.rsvpStatusOf(eventId, queue.get(1))).isEqualTo(RsvpStatus.CONFIRMED.name());
        assertThat(testData.waitlistPositions(eventId)).containsExactly(Map.entry(queue.get(2), 1));
    }

    @Test
    void raisingTheSeatLimitPastTheQueuePromotesEveryoneAndLeavesSeatsFree() {
        long eventId = event(EventMode.PHYSICAL);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, SEAT_LIMIT);
        testData.insertWaitlist(eventId, 1);

        MvcTestResult result = patch(eventId, seatLimit(SEAT_LIMIT + 3));

        assertThat(result).bodyJson().extractingPath("$.results[0].availableSeats").isEqualTo(2);
        assertThat(testData.waitlistPositions(eventId)).isEmpty();
    }

    // Promotion changes what the promoted person may see: the meeting link is
    // a live permission check, so it appears the moment they are confirmed.
    @Test
    void promotedAttendeeCanNowSeeTheMeetingLink() {
        long eventId = event(EventMode.ONLINE);
        long leaver = testData.insertUser();
        testData.insertRsvp(eventId, leaver, RsvpStatus.CONFIRMED);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);
        long nextInLine = testData.insertWaitlist(eventId, 1).get(0);
        assertThat(getAs(eventId, nextInLine)).bodyJson().extractingPath("$.results[0].meetingLink").isNull();

        cancelRsvpAs(eventId, leaver);

        assertThat(getAs(eventId, nextInLine)).bodyJson().extractingPath("$.results[0].meetingLink")
                .isEqualTo(TestData.MEETING_LINK);
    }

    private long event(EventMode mode) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Event", mode, EventStatus.UPCOMING,
                SEAT_LIMIT, NOW.plusWeeks(1));
    }

    private UpdateEventRequestDto seatLimit(int value) {
        return new UpdateEventRequestDto(null, null, null, null, null, null, value, null);
    }
}
