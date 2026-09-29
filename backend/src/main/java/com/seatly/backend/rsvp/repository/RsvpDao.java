package com.seatly.backend.rsvp.repository;

import com.seatly.backend.rsvp.model.Rsvp;
import com.seatly.backend.rsvp.model.Rsvp_;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RsvpDao extends JpaRepository<Rsvp, Long> {

    long countByEventIdAndStatus(Long eventId, RsvpStatus status);

    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, RsvpStatus status);

    // At most one row per (user, event) — the unique constraint in V1 — and
    // it may be CANCELLED, which is why callers check the status.
    Optional<Rsvp> findByEventIdAndUserId(Long eventId, Long userId);

    /** The queue in order — what promotion walks and renumbers. */
    List<Rsvp> findByEventIdAndStatusOrderByPositionAsc(Long eventId, RsvpStatus status);

    // "WithUser" is ignored by Spring Data's parser — it names the entity
    // graph: every row's user is needed for the response, so load them all
    // in the same query instead of one query per row.
    @EntityGraph(attributePaths = Rsvp_.USER)
    List<Rsvp> findWithUserByEventIdAndStatusOrderByPositionAsc(Long eventId, RsvpStatus status);

    @EntityGraph(attributePaths = Rsvp_.USER)
    List<Rsvp> findWithUserByEventIdAndStatusOrderByCreatedAtAsc(Long eventId, RsvpStatus status);

    @EntityGraph(attributePaths = Rsvp_.EVENT)
    List<Rsvp> findWithEventByUserIdAndStatusInOrderByEventEventDateAsc(Long userId, Collection<RsvpStatus> statuses);

    /**
     * One query for a whole page of events — the N+1 guard for seat counts.
     * An event with no RSVPs in the given status has no row in the result;
     * GROUP BY only returns groups that exist.
     */
    @Query("""
            SELECT new com.seatly.backend.rsvp.repository.EventRsvpCount(r.event.id, COUNT(r))
            FROM Rsvp r
            WHERE r.event.id IN :eventIds AND r.status = :status
            GROUP BY r.event.id
            """)
    List<EventRsvpCount> countPerEvent(
            @Param("eventIds") Collection<Long> eventIds, @Param("status") RsvpStatus status);
}
