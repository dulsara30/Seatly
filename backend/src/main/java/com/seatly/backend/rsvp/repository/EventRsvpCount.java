package com.seatly.backend.rsvp.repository;

/**
 * One row of a grouped RSVP count — built directly by the JPQL constructor
 * expression in RsvpDao, so callers get a typed value instead of Object[].
 */
public record EventRsvpCount(Long eventId, Long rsvpCount) {
}
