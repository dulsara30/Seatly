package com.seatly.backend.event;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class UpdateEventIntegrationTest extends AbstractIntegrationTest {

    private static final int SEAT_LIMIT = 10;
    private static final int CONFIRMED_COUNT = 5;
    private static final LocalDateTime NEXT_WEEK = NOW.plusWeeks(1);
    private static final String NEW_LINK = "https://meet.example.test/new";
    private static final String NEW_LOCATION = "The Commons, Colombo";

    @Test
    void updatesOnlyTheFieldsSent() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        MvcTestResult result = patch(eventId, changes().name("Renamed").build());

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].name").isEqualTo("Renamed");
        assertThat(result).bodyJson().extractingPath("$.results[0].description").isEqualTo("Test event");
        assertThat(result).bodyJson().extractingPath("$.results[0].seatLimit").isEqualTo(SEAT_LIMIT);
    }

    @Test
    void returnsNotFoundForUnknownEvent() {
        assertError(patch(Long.MAX_VALUE, changes().name("Renamed").build()),
                HttpStatus.NOT_FOUND, EventMessageKeys.NOT_FOUND);
    }

    @Test
    void returnsNotFoundForSoftDeletedEvent() {
        long eventId = ownEvent(EventMode.PHYSICAL);
        testData.softDeleteEvent(eventId);

        assertError(patch(eventId, changes().name("Renamed").build()),
                HttpStatus.NOT_FOUND, EventMessageKeys.NOT_FOUND);
    }

    // Organiser check

    @Test
    void forbidsUpdateByNonOrganiser() {
        long eventId = testData.insertEvent(testData.insertUser(), "Their Event", EventMode.PHYSICAL,
                EventStatus.UPCOMING, SEAT_LIMIT, NEXT_WEEK);

        assertError(patch(eventId, changes().name("Renamed").build()),
                HttpStatus.FORBIDDEN, EventMessageKeys.NOT_ORGANIZER);
    }

    // Who before state: a stranger must not learn that the event is cancelled.
    @Test
    void checksOrganiserBeforeEventStatus() {
        long eventId = testData.insertEvent(testData.insertUser(), "Their Event", EventMode.PHYSICAL,
                EventStatus.CANCELLED, SEAT_LIMIT, NEXT_WEEK);

        assertError(patch(eventId, changes().name("Renamed").build()),
                HttpStatus.FORBIDDEN, EventMessageKeys.NOT_ORGANIZER);
    }

    // Status check

    @Test
    void rejectsUpdateToCancelledEvent() {
        long eventId = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Cancelled", EventMode.PHYSICAL,
                EventStatus.CANCELLED, SEAT_LIMIT, NEXT_WEEK);

        assertError(patch(eventId, changes().name("Renamed").build()),
                HttpStatus.BAD_REQUEST, EventMessageKeys.NOT_UPCOMING);
    }

    // Seat limit against confirmed count

    @Test
    void rejectsSeatLimitBelowConfirmedCount() {
        long eventId = ownEventWithConfirmedRsvps();

        assertError(patch(eventId, changes().seatLimit(CONFIRMED_COUNT - 1).build()),
                HttpStatus.BAD_REQUEST, EventMessageKeys.SEAT_LIMIT_BELOW_CONFIRMED);
    }

    // The boundary: every confirmed person keeps their seat, zero seats left.
    @Test
    void acceptsSeatLimitEqualToConfirmedCount() {
        long eventId = ownEventWithConfirmedRsvps();

        MvcTestResult result = patch(eventId, changes().seatLimit(CONFIRMED_COUNT).build());

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].availableSeats").isEqualTo(0);
    }

    // Event date

    @Test
    void rejectsEventDateInThePast() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        assertError(patch(eventId, changes().eventDate(NOW.minusDays(1)).build()),
                HttpStatus.BAD_REQUEST, EventMessageKeys.DATE_MUST_BE_FUTURE);
    }

    @Test
    void acceptsNewFutureEventDate() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        assertThat(patch(eventId, changes().eventDate(NOW.plusMonths(1)).build())).hasStatusOk();
    }

    // Mode switch — the unused venue field is cleared in BOTH directions.

    @Test
    void clearsLocationWhenSwitchingPhysicalToOnline() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        MvcTestResult result = patch(eventId, changes().mode(EventMode.ONLINE).meetingLink(NEW_LINK).build());

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].location").isNull();
        assertThat(result).bodyJson().extractingPath("$.results[0].meetingLink").isEqualTo(NEW_LINK);
    }

    @Test
    void clearsMeetingLinkWhenSwitchingOnlineToPhysical() {
        long eventId = ownEvent(EventMode.ONLINE);

        MvcTestResult result = patch(eventId, changes().mode(EventMode.PHYSICAL).location(NEW_LOCATION).build());

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].meetingLink").isNull();
        assertThat(result).bodyJson().extractingPath("$.results[0].location").isEqualTo(NEW_LOCATION);
    }

    @Test
    void rejectsSwitchToOnlineWithoutMeetingLink() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        assertError(patch(eventId, changes().mode(EventMode.ONLINE).build()),
                HttpStatus.BAD_REQUEST, EventMessageKeys.MEETING_LINK_REQUIRED);
    }

    @Test
    void rejectsSwitchToPhysicalWithoutLocation() {
        long eventId = ownEvent(EventMode.ONLINE);

        assertError(patch(eventId, changes().mode(EventMode.PHYSICAL).build()),
                HttpStatus.BAD_REQUEST, EventMessageKeys.LOCATION_REQUIRED);
    }

    // The service modifies the entity before this check fails — the
    // transaction rollback must discard that. Readable here only because the
    // tests don't share a transaction with the request.
    @Test
    void leavesEventUnchangedWhenUpdateIsRejected() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        assertThat(patch(eventId, changes().name("Renamed").mode(EventMode.ONLINE).build()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        MvcTestResult after = get(eventId);
        assertThat(after).bodyJson().extractingPath("$.results[0].name").isEqualTo("Own Event");
        assertThat(after).bodyJson().extractingPath("$.results[0].mode").isEqualTo(EventMode.PHYSICAL.name());
        assertThat(after).bodyJson().extractingPath("$.results[0].location").isEqualTo(TestData.PHYSICAL_LOCATION);
    }

    // Tags: omitted = unchanged, empty = remove all, unknown id = rejected.

    @Test
    void keepsTagsWhenTagIdsOmitted() {
        long eventId = ownEvent(EventMode.PHYSICAL);
        testData.tagEvent(eventId, "tech");

        assertThat(patch(eventId, changes().name("Renamed").build()))
                .bodyJson().extractingPath("$.results[0].tags[0].name").isEqualTo("tech");
    }

    @Test
    void removesAllTagsWhenTagIdsEmpty() {
        long eventId = ownEvent(EventMode.PHYSICAL);
        testData.tagEvent(eventId, "tech");

        assertThat(patch(eventId, changes().tagIds(Set.of()).build()))
                .bodyJson().extractingPath("$.results[0].tags").asArray().isEmpty();
    }

    @Test
    void rejectsUnknownTagId() {
        long eventId = ownEvent(EventMode.PHYSICAL);

        assertError(patch(eventId, changes().tagIds(Set.of(Long.MAX_VALUE)).build()),
                HttpStatus.BAD_REQUEST, EventMessageKeys.TAG_NOT_FOUND);
    }

    private long ownEvent(EventMode mode) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Own Event", mode, EventStatus.UPCOMING,
                SEAT_LIMIT, NEXT_WEEK);
    }

    private long ownEventWithConfirmedRsvps() {
        long eventId = ownEvent(EventMode.PHYSICAL);
        testData.insertRsvps(eventId, RsvpStatus.CONFIRMED, CONFIRMED_COUNT);
        return eventId;
    }

    private static UpdateRequestBuilder changes() {
        return new UpdateRequestBuilder();
    }

    /** A PATCH body with only the named fields set; every other field is null ("unchanged"). */
    private static final class UpdateRequestBuilder {

        private String name;
        private EventMode mode;
        private String location;
        private String meetingLink;
        private LocalDateTime eventDate;
        private Integer seatLimit;
        private Set<Long> tagIds;

        UpdateRequestBuilder name(String value) {
            name = value;
            return this;
        }

        UpdateRequestBuilder mode(EventMode value) {
            mode = value;
            return this;
        }

        UpdateRequestBuilder location(String value) {
            location = value;
            return this;
        }

        UpdateRequestBuilder meetingLink(String value) {
            meetingLink = value;
            return this;
        }

        UpdateRequestBuilder eventDate(LocalDateTime value) {
            eventDate = value;
            return this;
        }

        UpdateRequestBuilder seatLimit(int value) {
            seatLimit = value;
            return this;
        }

        UpdateRequestBuilder tagIds(Set<Long> value) {
            tagIds = value;
            return this;
        }

        UpdateEventRequestDto build() {
            return new UpdateEventRequestDto(name, null, mode, location, meetingLink, eventDate, seatLimit, tagIds);
        }
    }
}
