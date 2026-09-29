package com.seatly.backend.event;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.common.constant.PaginationConstants;
import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class ListEventsIntegrationTest extends AbstractIntegrationTest {

    private static final String ITEM_NAMES = "$.results[0].items[*].name";

    private static final String SOONER = "Online Sooner";
    private static final String LATER = "Physical Later";

    private long laterEventId;

    // Two listable events, plus one cancelled and one deleted that must never appear.
    @BeforeEach
    void arrangeEvents() {
        testData.insertEvent(TestData.SEEDED_ORGANISER_ID, SOONER, EventMode.ONLINE, EventStatus.UPCOMING, 20,
                NOW.plusDays(1));
        laterEventId = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, LATER, EventMode.PHYSICAL,
                EventStatus.UPCOMING, 10, NOW.plusDays(3));
        testData.tagEvent(laterEventId, "tech");

        testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Cancelled", EventMode.ONLINE, EventStatus.CANCELLED, 10,
                NOW.plusDays(2));
        long deletedEventId = testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Deleted", EventMode.ONLINE,
                EventStatus.UPCOMING, 10, NOW.plusDays(2));
        testData.softDeleteEvent(deletedEventId);
    }

    @Test
    void listsOnlyUpcomingNonDeletedEventsSoonestFirst() {
        MvcTestResult result = mvc.get().uri(EVENTS_PATH).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly(SOONER, LATER);
        assertThat(result).bodyJson().extractingPath("$.results[0].totalItems").isEqualTo(2);
    }

    // The grouped count returns no row for an event with no confirmed RSVPs;
    // that event must still show every seat as available.
    @Test
    void derivesAvailableSeatsPerEventFromOneGroupedCount() {
        testData.insertRsvps(laterEventId, RsvpStatus.CONFIRMED, 3);
        testData.insertRsvps(laterEventId, RsvpStatus.WAITLISTED, 1);

        MvcTestResult result = mvc.get().uri(EVENTS_PATH).exchange();

        assertThat(result).bodyJson().extractingPath("$.results[0].items[0].availableSeats").isEqualTo(20);
        assertThat(result).bodyJson().extractingPath("$.results[0].items[1].availableSeats").isEqualTo(7);
    }

    @Test
    void filtersByTagIgnoringCase() {
        assertThat(mvc.get().uri(EVENTS_PATH).param("tag", "TECH").exchange())
                .bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly(LATER);
    }

    @Test
    void filtersByMode() {
        assertThat(mvc.get().uri(EVENTS_PATH).param("mode", EventMode.ONLINE.name()).exchange())
                .bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly(SOONER);
    }

    @Test
    void searchesNameCaseInsensitively() {
        assertThat(mvc.get().uri(EVENTS_PATH).param("search", "physical").exchange())
                .bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly(LATER);
    }

    // "%" is a LIKE wildcard. Unescaped, "100%" would also match "100 Remote".
    @Test
    void treatsLikeWildcardsInSearchAsLiteralText() {
        testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "100% Remote", EventMode.ONLINE, EventStatus.UPCOMING, 10,
                NOW.plusDays(5));
        testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "100 Remote", EventMode.ONLINE, EventStatus.UPCOMING, 10,
                NOW.plusDays(5));

        assertThat(mvc.get().uri(EVENTS_PATH).param("search", "100%").exchange())
                .bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly("100% Remote");
    }

    // An empty query parameter is trimmed to null — "no filter", not "tag ''".
    @Test
    void ignoresEmptyTagParameter() {
        assertThat(mvc.get().uri(EVENTS_PATH).param("tag", "").exchange())
                .bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly(SOONER, LATER);
    }

    @Test
    void pagesResults() {
        MvcTestResult result = mvc.get().uri(EVENTS_PATH).param("page", "1").param("size", "1").exchange();

        assertThat(result).bodyJson().extractingPath(ITEM_NAMES).asArray().containsExactly(LATER);
        assertThat(result).bodyJson().extractingPath("$.results[0].currentPage").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.results[0].totalPages").isEqualTo(2);
    }

    @Test
    void rejectsPageSizeAboveMaximum() {
        String tooLarge = String.valueOf(PaginationConstants.MAX_PAGE_SIZE + 1);

        assertError(mvc.get().uri(EVENTS_PATH).param("size", tooLarge).exchange(),
                HttpStatus.BAD_REQUEST, CommonMessageKeys.PAGE_SIZE_INVALID);
    }

    @Test
    void rejectsNegativePageNumber() {
        assertError(mvc.get().uri(EVENTS_PATH).param("page", "-1").exchange(),
                HttpStatus.BAD_REQUEST, CommonMessageKeys.PAGE_NUMBER_INVALID);
    }

    @Test
    void rejectsUnknownMode() {
        assertError(mvc.get().uri(EVENTS_PATH).param("mode", "online").exchange(),
                HttpStatus.BAD_REQUEST, CommonMessageKeys.INVALID_PARAMETER);
    }
}
