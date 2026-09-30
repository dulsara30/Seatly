package com.seatly.backend.event;

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
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** GET /v1/events/my and POST /v1/events/{id}/cancel — the organiser's dashboard and danger zone. */
class OrganiserEventsIntegrationTest extends AbstractIntegrationTest {

    private static final String MY_EVENTS_PATH = "/v1/events/my";
    private static final String CANCEL_PATH = "/v1/events/{eventId}/cancel";
    private static final String MY_RSVPS_PATH = "/v1/rsvps/my";
    private static final int SEAT_LIMIT = 10;

    // ---- my events ----

    @Test
    void listsOnlyTheCallersEventsInEveryStatusSoonestFirst() {
        insertOwnEvent("Later", EventStatus.UPCOMING, 5);
        insertOwnEvent("Sooner Cancelled", EventStatus.CANCELLED, 1);
        long deleted = insertOwnEvent("Deleted", EventStatus.UPCOMING, 3);
        testData.softDeleteEvent(deleted);
        testData.insertEvent(testData.insertUser(), "Someone Else's", EventMode.PHYSICAL, EventStatus.UPCOMING,
                SEAT_LIMIT, NOW.plusDays(2));

        MvcTestResult result = getPathAs(MY_EVENTS_PATH, TestData.SEEDED_ORGANISER_ID);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[*].name").asArray()
                .containsExactly("Sooner Cancelled", "Later");
    }

    @Test
    void derivesAvailableSeatsForEachOfMyEvents() {
        long eventId = insertOwnEvent("Half Full", EventStatus.UPCOMING, 5);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 4);

        assertThat(getPathAs(MY_EVENTS_PATH, TestData.SEEDED_ORGANISER_ID))
                .bodyJson().extractingPath("$.results[0].availableSeats").isEqualTo(SEAT_LIMIT - 4);
    }

    // /v1/events/my is one path segment, so the public "GET /v1/events/*"
    // rule matches it too. This is what listing it first in SecurityConfig
    // protects — and it must be a 401, not a 400 from parsing "my" as an id.
    @Test
    void requiresTokenEvenThoughEventDetailIsPublic() {
        assertError(mvc.get().uri(MY_EVENTS_PATH).exchange(),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    // ---- cancel ----

    @Test
    void organiserCancelsTheirEvent() {
        long eventId = insertOwnEvent("To Cancel", EventStatus.UPCOMING, 5);

        MvcTestResult result = cancelAs(eventId, TestData.SEEDED_ORGANISER_ID);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].status").isEqualTo(EventStatus.CANCELLED.name());
        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].status").isEqualTo(EventStatus.CANCELLED.name());
    }

    @Test
    void cancelledEventStopsAcceptingRsvps() {
        long eventId = insertOwnEvent("To Cancel", EventStatus.UPCOMING, 5);
        cancelAs(eventId, TestData.SEEDED_ORGANISER_ID);

        assertError(rsvpAs(eventId, testData.insertUser()), HttpStatus.BAD_REQUEST, RsvpMessageKeys.EVENT_NOT_UPCOMING);
    }

    // RSVP rows are kept (they're who to notify), so an attendee's list must
    // say the event is cancelled rather than still showing "Confirmed".
    @Test
    void attendeeSeesTheCancellationOnTheirRsvps() {
        long eventId = insertOwnEvent("To Cancel", EventStatus.UPCOMING, 5);
        long attendee = testData.insertUser();
        rsvpAs(eventId, attendee);

        cancelAs(eventId, TestData.SEEDED_ORGANISER_ID);

        MvcTestResult myRsvps = getPathAs(MY_RSVPS_PATH, attendee);
        assertThat(myRsvps).bodyJson().extractingPath("$.results[0].eventStatus").isEqualTo(EventStatus.CANCELLED.name());
        assertThat(myRsvps).bodyJson().extractingPath("$.results[0].status").isEqualTo(RsvpStatus.CONFIRMED.name());
    }

    @Test
    void forbidsCancelByNonOrganiser() {
        long eventId = insertOwnEvent("Not Yours", EventStatus.UPCOMING, 5);

        assertError(cancelAs(eventId, testData.insertUser()), HttpStatus.FORBIDDEN, EventMessageKeys.NOT_ORGANIZER);
    }

    @Test
    void rejectsCancellingTwice() {
        long eventId = insertOwnEvent("Already Cancelled", EventStatus.CANCELLED, 5);

        assertError(cancelAs(eventId, TestData.SEEDED_ORGANISER_ID), HttpStatus.BAD_REQUEST, EventMessageKeys.NOT_UPCOMING);
    }

    @Test
    void rejectsCancelOfUnknownEvent() {
        assertError(cancelAs(Long.MAX_VALUE, TestData.SEEDED_ORGANISER_ID), HttpStatus.NOT_FOUND, EventMessageKeys.NOT_FOUND);
    }

    @Test
    void rejectsCancelWithoutToken() {
        long eventId = insertOwnEvent("To Cancel", EventStatus.UPCOMING, 5);

        assertError(mvc.post().uri(CANCEL_PATH, eventId).exchange(),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    private long insertOwnEvent(String name, EventStatus status, int daysAhead) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, name, EventMode.PHYSICAL, status, SEAT_LIMIT,
                NOW.plusDays(daysAhead));
    }

    private MvcTestResult cancelAs(long eventId, long userId) {
        return mvc.post().uri(CANCEL_PATH, eventId).header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId)).exchange();
    }
}
