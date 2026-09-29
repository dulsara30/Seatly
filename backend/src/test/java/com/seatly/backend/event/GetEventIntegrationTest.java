package com.seatly.backend.event;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class GetEventIntegrationTest extends AbstractIntegrationTest {

    private static final int SEAT_LIMIT = 10;
    private static final LocalDateTime NEXT_WEEK = NOW.plusWeeks(1);

    @Test
    void returnsEventWithDescription() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        MvcTestResult result = get(eventId);

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].id").convertTo(Long.class).isEqualTo(eventId);
        assertThat(result).bodyJson().extractingPath("$.results[0].description").isEqualTo("Test event");
    }

    // Only CONFIRMED RSVPs take a seat — waitlisted and cancelled ones don't.
    @Test
    void availableSeatsCountOnlyConfirmedRsvps() {
        long eventId = ownEvent(EventMode.PHYSICAL);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, 3);
        testData.insertRsvps(eventId, RsvpStatus.WAITLISTED, 2);
        testData.insertRsvps(eventId, RsvpStatus.CANCELLED, 1);

        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].availableSeats").isEqualTo(SEAT_LIMIT - 3);
    }

    // Detail is not limited to UPCOMING — a cancelled event can still be viewed.
    @Test
    void returnsCancelledEvent() {
        long eventId = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Cancelled", EventMode.PHYSICAL,
                EventStatus.CANCELLED, SEAT_LIMIT, NEXT_WEEK);

        assertThat(get(eventId)).hasStatusOk();
    }

    @Test
    void returnsNotFoundForUnknownId() {
        assertError(get(Long.MAX_VALUE), HttpStatus.NOT_FOUND, EventMessageKeys.NOT_FOUND);
    }

    @Test
    void returnsNotFoundForSoftDeletedEvent() {
        long eventId = ownEvent(EventMode.PHYSICAL);
        testData.softDeleteEvent(eventId);

        assertError(get(eventId), HttpStatus.NOT_FOUND, EventMessageKeys.NOT_FOUND);
    }

    @Test
    void rejectsNonNumericId() {
        assertError(mvc.get().uri("/v1/events/abc").exchange(), HttpStatus.BAD_REQUEST,
                CommonMessageKeys.INVALID_PARAMETER);
    }

    // meetingLink — a live permission check, not public data.

    @Test
    void showsMeetingLinkToOrganiser() {
        long eventId = ownEvent(EventMode.ONLINE);

        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].meetingLink").isEqualTo(TestData.MEETING_LINK);
    }

    @Test
    void hidesMeetingLinkFromCallerWithoutRsvp() {
        long eventId = someoneElsesOnlineEvent();

        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].meetingLink").isNull();
    }

    @Test
    void showsMeetingLinkToConfirmedAttendee() {
        long eventId = someoneElsesOnlineEvent();
        testData.insertRsvp(eventId, TestData.SEEDED_ORGANISER_ID, RsvpStatus.CONFIRMED);

        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].meetingLink").isEqualTo(TestData.MEETING_LINK);
    }

    @Test
    void hidesMeetingLinkFromWaitlistedAttendee() {
        long eventId = someoneElsesOnlineEvent();
        testData.insertRsvp(eventId, TestData.SEEDED_ORGANISER_ID, RsvpStatus.WAITLISTED);

        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].meetingLink").isNull();
    }

    private long ownEvent(EventMode mode) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Own Event", mode, EventStatus.UPCOMING,
                SEAT_LIMIT, NEXT_WEEK);
    }

    private long someoneElsesOnlineEvent() {
        return testData.insertEvent(testData.insertUser(), "Their Event", EventMode.ONLINE, EventStatus.UPCOMING,
                SEAT_LIMIT, NEXT_WEEK);
    }
}
