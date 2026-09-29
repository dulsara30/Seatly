package com.seatly.backend.event;

import static com.seatly.backend.support.FixedClockConfiguration.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.auth.payload.LoginRequestDto;
import com.seatly.backend.auth.payload.RegisterRequestDto;
import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.event.payload.CreateEventRequestDto;
import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.event.type.EventMessageKeys;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/**
 * Who may call what on /v1/events, and that the caller really comes from the
 * token now — not from a hardcoded id.
 */
class EventSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final int SEAT_LIMIT = 10;

    @Test
    void listsEventsWithoutToken() {
        assertThat(mvc.get().uri(EVENTS_PATH).exchange()).hasStatusOk();
    }

    @Test
    void showsEventWithoutToken() {
        assertThat(getAnonymously(organisersEvent(EventMode.PHYSICAL))).hasStatusOk();
    }

    // Public to read — but the meeting link is still only for the organiser
    // and confirmed attendees, and an anonymous visitor is neither.
    @Test
    void hidesMeetingLinkFromAnonymousVisitor() {
        long eventId = organisersEvent(EventMode.ONLINE);

        assertThat(getAnonymously(eventId)).bodyJson().extractingPath("$.results[0].meetingLink").isNull();
        assertThat(get(eventId)).bodyJson().extractingPath("$.results[0].meetingLink").isEqualTo(TestData.MEETING_LINK);
    }

    @Test
    void rejectsCreateWithoutToken() {
        assertError(postAnonymously(EVENTS_PATH, newEvent()),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    // A bad token is ignored, not trusted — on a protected endpoint that
    // leaves the request unauthenticated, so it's a 401 like no token at all.
    @Test
    void rejectsCreateWithInvalidToken() {
        MvcTestResult result = mvc.post().uri(EVENTS_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                .contentType(MediaType.APPLICATION_JSON).content(toJson(newEvent())).exchange();

        assertError(result, HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    @Test
    void rejectsUpdateWithoutToken() {
        long eventId = organisersEvent(EventMode.PHYSICAL);
        MvcTestResult result = mvc.patch().uri(EVENT_PATH, eventId).contentType(MediaType.APPLICATION_JSON)
                .content(toJson(renameTo("Renamed"))).exchange();

        assertError(result, HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    // The end-to-end proof that CURRENT_USER_ID is gone: a brand-new account,
    // logged in over HTTP, becomes the organiser of what it creates — and the
    // seeded organiser (the old hardcoded id 1) is then locked out of it.
    @Test
    void makesTheTokensUserTheOrganiser() {
        postAnonymously("/v1/auth/register",
                new RegisterRequestDto("New Organiser", "new.organiser@seatly.test", "a-long-password", null));
        String token = readJson(postAnonymously("/v1/auth/login",
                new LoginRequestDto("new.organiser@seatly.test", "a-long-password")), "$.results[0].accessToken");
        long newUserId = testData.userIdByEmail("new.organiser@seatly.test");

        MvcTestResult created = mvc.post().uri(EVENTS_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(toJson(newEvent())).exchange();

        assertThat(created).hasStatus(HttpStatus.CREATED);
        assertThat(created).bodyJson().extractingPath("$.results[0].organizer.id")
                .convertTo(Long.class).isEqualTo(newUserId);
        long eventId = ((Number) readJson(created, "$.results[0].id")).longValue();
        assertError(patchAs(eventId, renameTo("Taken over"), TestData.SEEDED_ORGANISER_ID),
                HttpStatus.FORBIDDEN, EventMessageKeys.NOT_ORGANIZER);
    }

    @Test
    void servesSwaggerWithoutTokenAndAdvertisesBearerAuth() {
        MvcTestResult apiDocs = mvc.get().uri("/v3/api-docs").exchange();

        assertThat(apiDocs).hasStatusOk();
        assertThat(apiDocs).bodyJson().extractingPath("$.components.securitySchemes.bearerAuth.scheme")
                .isEqualTo("bearer");
    }

    private long organisersEvent(EventMode mode) {
        return testData.insertEvent(TestData.SEEDED_ORGANISER_ID, "Organiser's Event", mode, EventStatus.UPCOMING,
                SEAT_LIMIT, NOW.plusWeeks(1));
    }

    private CreateEventRequestDto newEvent() {
        return new CreateEventRequestDto("New Event", "Description", EventMode.PHYSICAL, TestData.PHYSICAL_LOCATION,
                null, NOW.plusWeeks(1), SEAT_LIMIT, Set.of());
    }

    private UpdateEventRequestDto renameTo(String name) {
        return new UpdateEventRequestDto(name, null, null, null, null, null, null, null);
    }
}
