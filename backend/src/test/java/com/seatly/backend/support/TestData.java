package com.seatly.backend.support;

import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Arranges database state the API can't create yet — other users, RSVPs,
 * cancelled or deleted events. Plain SQL, so a test's setup never depends on
 * the code it is testing.
 */
public class TestData {

    /** Seeded by V2__seed_data.sql; matches EventServiceImpl's placeholder caller. */
    public static final long CURRENT_USER_ID = 1L;

    public static final String PHYSICAL_LOCATION = "Trace Expert City, Colombo";
    public static final String MEETING_LINK = "https://meet.example.test/seatly";

    private static final String NOT_A_REAL_PASSWORD_HASH = "not-a-real-hash";

    private final JdbcTemplate jdbc;

    public TestData(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Keeps the seeded caller and tags, removes everything tests create —
     * including V2's demo events, so list assertions see only their own rows.
     */
    public void reset() {
        jdbc.update("DELETE FROM rsvp");
        jdbc.update("DELETE FROM event_tag");
        jdbc.update("DELETE FROM event");
        jdbc.update("DELETE FROM email_outbox");
        jdbc.update("DELETE FROM app_user WHERE id <> ?", CURRENT_USER_ID);
    }

    public long insertUser() {
        return jdbc.queryForObject(
                "INSERT INTO app_user (name, email, password) VALUES (?, ?, ?) RETURNING id",
                Long.class, "Test User", UUID.randomUUID() + "@seatly.test", NOT_A_REAL_PASSWORD_HASH);
    }

    public long insertEvent(long organizerId, String name, EventMode mode, EventStatus status,
            int seatLimit, LocalDateTime eventDate) {
        String location = mode == EventMode.PHYSICAL ? PHYSICAL_LOCATION : null;
        String meetingLink = mode == EventMode.ONLINE ? MEETING_LINK : null;
        return jdbc.queryForObject("""
                        INSERT INTO event (organizer_id, name, description, mode, location, meeting_link,
                                           event_date, seat_limit, status)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        RETURNING id
                        """,
                Long.class, organizerId, name, "Test event", mode.name(), location, meetingLink,
                eventDate, seatLimit, status.name());
    }

    public void insertRsvp(long eventId, long userId, RsvpStatus status) {
        jdbc.update("INSERT INTO rsvp (user_id, event_id, status) VALUES (?, ?, ?)", userId, eventId, status.name());
    }

    /** One RSVP each from a fresh user — (user_id, event_id) is unique. */
    public void insertRsvps(long eventId, RsvpStatus status, int count) {
        for (int i = 0; i < count; i++) {
            insertRsvp(eventId, insertUser(), status);
        }
    }

    public void tagEvent(long eventId, String tagName) {
        jdbc.update("INSERT INTO event_tag (event_id, tag_id) VALUES (?, ?)", eventId, tagId(tagName));
    }

    public long tagId(String tagName) {
        return jdbc.queryForObject("SELECT id FROM tag WHERE name = ?", Long.class, tagName);
    }

    public void softDeleteEvent(long eventId) {
        jdbc.update("UPDATE event SET is_deleted = TRUE WHERE id = ?", eventId);
    }
}
