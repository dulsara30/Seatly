package com.seatly.backend.event;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.event.payload.CreateEventRequestDto;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class CreateEventIntegrationTest extends AbstractIntegrationTest {

    private static final int SEAT_LIMIT = 30;
    private static final LocalDateTime NEXT_WEEK = NOW.plusWeeks(1);

    @Test
    void createsEventWithCallerAsOrganiserAndAllSeatsAvailable() {
        long techTagId = testData.tagId("tech");

        MvcTestResult result = post(EVENTS_PATH, physicalEvent(NEXT_WEEK, Set.of(techTagId)));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.results[0].status").isEqualTo(EventStatus.UPCOMING.name());
        assertThat(result).bodyJson().extractingPath("$.results[0].organizer.id")
                .convertTo(Long.class).isEqualTo(TestData.SEEDED_ORGANISER_ID);
        assertThat(result).bodyJson().extractingPath("$.results[0].availableSeats").isEqualTo(SEAT_LIMIT);
        assertThat(result).bodyJson().extractingPath("$.results[0].tags[0].name").isEqualTo("tech");
    }

    @Test
    void rejectsEventDateInThePast() {
        assertError(post(EVENTS_PATH, physicalEvent(NOW.minusDays(1), Set.of())),
                HttpStatus.BAD_REQUEST, EventMessageKeys.DATE_MUST_BE_FUTURE);
    }

    // The boundary: "in the future" is strictly after now. Only testable
    // because the Clock is frozen — with a live clock "now" has already moved.
    @Test
    void rejectsEventDateExactlyNow() {
        assertError(post(EVENTS_PATH, physicalEvent(NOW, Set.of())),
                HttpStatus.BAD_REQUEST, EventMessageKeys.DATE_MUST_BE_FUTURE);
    }

    @Test
    void acceptsEventDateOneMinuteFromNow() {
        assertThat(post(EVENTS_PATH, physicalEvent(NOW.plusMinutes(1), Set.of()))).hasStatus(HttpStatus.CREATED);
    }

    @Test
    void rejectsOnlineEventWithoutMeetingLink() {
        CreateEventRequestDto request = new CreateEventRequestDto(
                "Online Talk", "Description", EventMode.ONLINE, null, null, NEXT_WEEK, SEAT_LIMIT, Set.of());

        assertError(post(EVENTS_PATH, request), HttpStatus.BAD_REQUEST, EventMessageKeys.MEETING_LINK_REQUIRED);
    }

    @Test
    void rejectsPhysicalEventWithoutLocation() {
        CreateEventRequestDto request = new CreateEventRequestDto(
                "Meetup", "Description", EventMode.PHYSICAL, null, null, NEXT_WEEK, SEAT_LIMIT, Set.of());

        assertError(post(EVENTS_PATH, request), HttpStatus.BAD_REQUEST, EventMessageKeys.LOCATION_REQUIRED);
    }

    // Sending both venue fields is accepted, but only the one the mode uses is kept.
    @Test
    void dropsLocationWhenCreatingOnlineEvent() {
        CreateEventRequestDto request = new CreateEventRequestDto("Online Talk", "Description", EventMode.ONLINE,
                TestData.PHYSICAL_LOCATION, TestData.MEETING_LINK, NEXT_WEEK, SEAT_LIMIT, Set.of());

        MvcTestResult result = post(EVENTS_PATH, request);

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.results[0].location").isNull();
        assertThat(result).bodyJson().extractingPath("$.results[0].meetingLink").isEqualTo(TestData.MEETING_LINK);
    }

    @Test
    void rejectsUnknownTagId() {
        long unknownTagId = Long.MAX_VALUE;

        assertError(post(EVENTS_PATH, physicalEvent(NEXT_WEEK, Set.of(unknownTagId))),
                HttpStatus.BAD_REQUEST, EventMessageKeys.TAG_NOT_FOUND);
    }

    // Shape validation — the DTO's job, surfaced through the same envelope.

    @Test
    void rejectsBlankName() {
        CreateEventRequestDto request = new CreateEventRequestDto("   ", "Description", EventMode.PHYSICAL,
                TestData.PHYSICAL_LOCATION, null, NEXT_WEEK, SEAT_LIMIT, Set.of());

        assertError(post(EVENTS_PATH, request), HttpStatus.BAD_REQUEST, EventMessageKeys.NAME_REQUIRED);
    }

    @Test
    void rejectsSeatLimitBelowOne() {
        CreateEventRequestDto request = new CreateEventRequestDto("Meetup", "Description", EventMode.PHYSICAL,
                TestData.PHYSICAL_LOCATION, null, NEXT_WEEK, 0, Set.of());

        assertError(post(EVENTS_PATH, request), HttpStatus.BAD_REQUEST, EventMessageKeys.SEAT_LIMIT_MIN);
    }

    @Test
    void rejectsMissingTagIds() {
        CreateEventRequestDto request = new CreateEventRequestDto("Meetup", "Description", EventMode.PHYSICAL,
                TestData.PHYSICAL_LOCATION, null, NEXT_WEEK, SEAT_LIMIT, null);

        assertError(post(EVENTS_PATH, request), HttpStatus.BAD_REQUEST, EventMessageKeys.TAG_IDS_REQUIRED);
    }

    @Test
    void rejectsMalformedJson() {
        // Authenticated: security runs before the body is parsed, so without a
        // token this would be a 401 and never reach the JSON check.
        MvcTestResult result = mvc.post().uri(EVENTS_PATH)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(TestData.SEEDED_ORGANISER_ID))
                .contentType(MediaType.APPLICATION_JSON).content("{ not json").exchange();

        assertError(result, HttpStatus.BAD_REQUEST, CommonMessageKeys.MALFORMED_REQUEST_BODY);
    }

    private CreateEventRequestDto physicalEvent(LocalDateTime eventDate, Set<Long> tagIds) {
        return new CreateEventRequestDto("Colombo Meetup", "Description", EventMode.PHYSICAL,
                TestData.PHYSICAL_LOCATION, null, eventDate, SEAT_LIMIT, tagIds);
    }
}
