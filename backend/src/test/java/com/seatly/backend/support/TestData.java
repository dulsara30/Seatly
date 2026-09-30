package com.seatly.backend.support;

import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

public class TestData {

    public static final long SEEDED_ORGANISER_ID = 1L;
    public static final String SEEDED_ORGANISER_EMAIL = "organiser@seatly.dev";
    public static final String SEEDED_ATTENDEE_EMAIL = "attendee@seatly.dev";
    // Dev-only seed password from V2/V3, not a secret.
    public static final String SEEDED_PASSWORD = "seatly-dev-password";

    public static final String PHYSICAL_LOCATION = "Trace Expert City, Colombo";
    public static final String MEETING_LINK = "https://meet.example.test/seatly";

    private static final String NOT_A_REAL_PASSWORD_HASH = "not-a-real-hash";

    private final JdbcTemplate jdbc;

    public TestData(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Also restores the seeded accounts, since some tests deactivate or delete them.
    public void reset() {
        jdbc.update("DELETE FROM rsvp");
        jdbc.update("DELETE FROM event_tag");
        jdbc.update("DELETE FROM event");
        jdbc.update("DELETE FROM email_outbox");
        jdbc.update("DELETE FROM app_user WHERE email NOT IN (?, ?)", SEEDED_ORGANISER_EMAIL, SEEDED_ATTENDEE_EMAIL);
        jdbc.update("UPDATE app_user SET is_active = TRUE, is_deleted = FALSE WHERE email IN (?, ?)",
                SEEDED_ORGANISER_EMAIL, SEEDED_ATTENDEE_EMAIL);
    }

    public long userIdByEmail(String email) {
        return jdbc.queryForObject("SELECT id FROM app_user WHERE email = ?", Long.class, email);
    }

    public String passwordHashOf(String email) {
        return jdbc.queryForObject("SELECT password FROM app_user WHERE email = ?", String.class, email);
    }

    public void softDeleteUser(long userId) {
        jdbc.update("UPDATE app_user SET is_deleted = TRUE WHERE id = ?", userId);
    }

    public void deactivateUser(long userId) {
        jdbc.update("UPDATE app_user SET is_active = FALSE WHERE id = ?", userId);
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

    public List<Long> insertWaitlist(long eventId, int count) {
        List<Long> userIds = new ArrayList<>();
        for (int position = 1; position <= count; position++) {
            long userId = insertUser();
            jdbc.update("INSERT INTO rsvp (user_id, event_id, status, position) VALUES (?, ?, ?, ?)",
                    userId, eventId, RsvpStatus.WAITLISTED.name(), position);
            userIds.add(userId);
        }
        return userIds;
    }

    public long countRsvps(long eventId, RsvpStatus status) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM rsvp WHERE event_id = ? AND status = ?",
                Long.class, eventId, status.name());
    }

    public String rsvpStatusOf(long eventId, long userId) {
        return jdbc.queryForObject("SELECT status FROM rsvp WHERE event_id = ? AND user_id = ?",
                String.class, eventId, userId);
    }

    public Map<Long, Integer> waitlistPositions(long eventId) {
        Map<Long, Integer> positions = new LinkedHashMap<>();
        jdbc.query("SELECT user_id, position FROM rsvp WHERE event_id = ? AND status = ? ORDER BY position",
                row -> {
                    positions.put(row.getLong("user_id"), row.getInt("position"));
                },
                eventId, RsvpStatus.WAITLISTED.name());
        return positions;
    }

    // A fresh user per RSVP: (user_id, event_id) is unique.
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
