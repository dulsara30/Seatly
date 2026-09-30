package com.seatly.backend.rsvp;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpMessageKeys;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class RsvpIntegrationTest extends AbstractIntegrationTest {

    private static final String ATTENDEES_PATH = "/v1/events/{eventId}/attendees";
    private static final String WAITLIST_PATH = "/v1/events/{eventId}/waitlist";
    private static final String MY_RSVPS_PATH = "/v1/rsvps/my";

    @Test
    void confirmsWhenASeatIsFree() {
        long eventId = eventWithSeatLimit(10);
        long userId = testData.insertUser();

        MvcTestResult result = rsvpAs(eventId, userId);

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.results[0].status").isEqualTo(RsvpStatus.CONFIRMED.name());
        assertThat(result).bodyJson().extractingPath("$.results[0].position").isNull();
        assertThat(testData.rsvpStatusOf(eventId, userId)).isEqualTo(RsvpStatus.CONFIRMED.name());
    }

    @Test
    void waitlistsWhenFullWithTheNextPosition() {
        long eventId = eventWithSeatLimit(1);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);

        MvcTestResult first = rsvpAs(eventId, testData.insertUser());
        MvcTestResult second = rsvpAs(eventId, testData.insertUser());

        assertThat(first).hasStatus(HttpStatus.CREATED);
        assertThat(first).bodyJson().extractingPath("$.results[0].status").isEqualTo(RsvpStatus.WAITLISTED.name());
        assertThat(first).bodyJson().extractingPath("$.results[0].position").isEqualTo(1);
        assertThat(second).bodyJson().extractingPath("$.results[0].position").isEqualTo(2);
    }

    @Test
    void rejectsRsvpToOwnEvent() {
        assertError(rsvpAs(eventWithSeatLimit(10), TestData.SEEDED_ORGANISER_ID),
                HttpStatus.BAD_REQUEST, RsvpMessageKeys.OWN_EVENT);
    }

    @Test
    void rejectsRsvpToEventThatIsNotUpcoming() {
        long eventId = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Cancelled", EventMode.PHYSICAL,
                EventStatus.CANCELLED, 10, NOW.plusWeeks(1));

        assertError(rsvpAs(eventId, testData.insertUser()), HttpStatus.BAD_REQUEST, RsvpMessageKeys.EVENT_NOT_UPCOMING);
    }

    @Test
    void rejectsSecondRsvpWhileConfirmed() {
        long eventId = eventWithSeatLimit(10);
        long userId = testData.insertUser();
        rsvpAs(eventId, userId);

        assertError(rsvpAs(eventId, userId), HttpStatus.CONFLICT, RsvpMessageKeys.ALREADY_EXISTS);
    }

    @Test
    void rejectsSecondRsvpWhileWaitlisted() {
        long eventId = eventWithSeatLimit(1);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 1);
        long userId = testData.insertUser();
        rsvpAs(eventId, userId);

        assertError(rsvpAs(eventId, userId), HttpStatus.CONFLICT, RsvpMessageKeys.ALREADY_EXISTS);
    }

    // (user_id, event_id) is unique, so re-RSVPing must reuse the cancelled row.
    @Test
    void allowsRsvpAgainAfterCancelling() {
        long eventId = eventWithSeatLimit(10);
        long userId = testData.insertUser();
        rsvpAs(eventId, userId);
        cancelRsvpAs(eventId, userId);

        MvcTestResult again = rsvpAs(eventId, userId);

        assertThat(again).hasStatus(HttpStatus.CREATED);
        assertThat(again).bodyJson().extractingPath("$.results[0].status").isEqualTo(RsvpStatus.CONFIRMED.name());
    }

    @Test
    void rejectsRsvpToUnknownEvent() {
        assertError(rsvpAs(Long.MAX_VALUE, testData.insertUser()), HttpStatus.NOT_FOUND, EventMessageKeys.NOT_FOUND);
    }

    @Test
    void rejectsRsvpWithoutToken() {
        assertError(mvc.post().uri(RSVP_PATH, eventWithSeatLimit(10)).exchange(),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    @Test
    void cancelReturnsTheCancelledRsvp() {
        long eventId = eventWithSeatLimit(10);
        long userId = testData.insertUser();
        rsvpAs(eventId, userId);

        MvcTestResult result = cancelRsvpAs(eventId, userId);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].status").isEqualTo(RsvpStatus.CANCELLED.name());
    }

    @Test
    void rejectsCancelWithoutAnRsvp() {
        assertError(cancelRsvpAs(eventWithSeatLimit(10), testData.insertUser()),
                HttpStatus.NOT_FOUND, RsvpMessageKeys.NOT_FOUND);
    }

    @Test
    void rejectsCancellingTwice() {
        long eventId = eventWithSeatLimit(10);
        long userId = testData.insertUser();
        rsvpAs(eventId, userId);
        cancelRsvpAs(eventId, userId);

        assertError(cancelRsvpAs(eventId, userId), HttpStatus.NOT_FOUND, RsvpMessageKeys.NOT_FOUND);
    }

    @Test
    void showsConfirmedAttendeesToOrganiser() {
        long eventId = eventWithSeatLimit(10);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 2);
        testData.insertWaitlist(eventId, 1);

        MvcTestResult result = getPathAs(ATTENDEES_PATH, TestData.SEEDED_ORGANISER_ID, eventId);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].items").asArray().hasSize(2);
        assertThat(result).bodyJson().extractingPath("$.results[0].confirmedCount").isEqualTo(2);
        assertThat(result).bodyJson().extractingPath("$.results[0].seatLimit").isEqualTo(10);
    }

    @Test
    void showsWaitlistToOrganiserInQueueOrder() {
        long eventId = eventWithSeatLimit(1);
        List<Long> queue = testData.insertWaitlist(eventId, 3);

        MvcTestResult result = getPathAs(WAITLIST_PATH, TestData.SEEDED_ORGANISER_ID, eventId);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[*].position").asArray().containsExactly(1, 2, 3);
        assertThat(result).bodyJson().extractingPath("$.results[0].userId").convertTo(Long.class).isEqualTo(queue.get(0));
    }

    @Test
    void forbidsAttendeeListToNonOrganiser() {
        assertError(getPathAs(ATTENDEES_PATH, testData.insertUser(), eventWithSeatLimit(10)),
                HttpStatus.FORBIDDEN, EventMessageKeys.NOT_ORGANIZER);
    }

    @Test
    void forbidsWaitlistToNonOrganiser() {
        assertError(getPathAs(WAITLIST_PATH, testData.insertUser(), eventWithSeatLimit(10)),
                HttpStatus.FORBIDDEN, EventMessageKeys.NOT_ORGANIZER);
    }

    @Test
    void rejectsAttendeeListWithoutToken() {
        assertError(mvc.get().uri(ATTENDEES_PATH, eventWithSeatLimit(10)).exchange(),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    @Test
    void listsMyActiveRsvpsSoonestFirstWithoutCancelledOnes() {
        long userId = testData.insertUser();
        long later = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Later", EventMode.PHYSICAL,
                EventStatus.UPCOMING, 10, NOW.plusDays(9));
        long soonerAndFull = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Sooner", EventMode.PHYSICAL,
                EventStatus.UPCOMING, 1, NOW.plusDays(2));
        long cancelled = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Cancelled Mine", EventMode.PHYSICAL,
                EventStatus.UPCOMING, 10, NOW.plusDays(5));
        testData.insertRsvps(soonerAndFull, RsvpStatus.CONFIRMED, 1);
        rsvpAs(later, userId);
        rsvpAs(soonerAndFull, userId);
        rsvpAs(cancelled, userId);
        cancelRsvpAs(cancelled, userId);

        MvcTestResult result = getPathAs(MY_RSVPS_PATH, userId);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[*].eventName").asArray()
                .containsExactly("Sooner", "Later");
        assertThat(result).bodyJson().extractingPath("$.results[0].status").isEqualTo(RsvpStatus.WAITLISTED.name());
        assertThat(result).bodyJson().extractingPath("$.results[0].position").isEqualTo(1);
    }

    @Test
    void rejectsMyRsvpsWithoutToken() {
        assertError(mvc.get().uri(MY_RSVPS_PATH).exchange(),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    private long eventWithSeatLimit(int seatLimit) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Event", EventMode.PHYSICAL,
                EventStatus.UPCOMING, seatLimit, NOW.plusWeeks(1));
    }
}
