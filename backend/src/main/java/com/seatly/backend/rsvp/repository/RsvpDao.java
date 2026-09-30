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

    // The single row may be CANCELLED, so callers check the status.
    Optional<Rsvp> findByEventIdAndUserId(Long eventId, Long userId);

    List<Rsvp> findByEventIdAndStatusOrderByPositionAsc(Long eventId, RsvpStatus status);

    // "WithUser" is ignored by the parser; the entity graph loads every user in the same query.
    @EntityGraph(attributePaths = Rsvp_.USER)
    List<Rsvp> findWithUserByEventIdAndStatusOrderByPositionAsc(Long eventId, RsvpStatus status);

    @EntityGraph(attributePaths = Rsvp_.USER)
    List<Rsvp> findWithUserByEventIdAndStatusOrderByCreatedAtAsc(Long eventId, RsvpStatus status);

    @EntityGraph(attributePaths = Rsvp_.EVENT)
    List<Rsvp> findWithEventByUserIdAndStatusInOrderByEventEventDateAsc(Long userId, Collection<RsvpStatus> statuses);

    @Query("""
            SELECT new com.seatly.backend.rsvp.repository.EventRsvpCount(r.event.id, COUNT(r))
            FROM Rsvp r
            WHERE r.event.id IN :eventIds AND r.status = :status
            GROUP BY r.event.id
            """)
    List<EventRsvpCount> countPerEvent(
            @Param("eventIds") Collection<Long> eventIds, @Param("status") RsvpStatus status);
}
