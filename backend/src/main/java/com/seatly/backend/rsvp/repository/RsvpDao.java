package com.seatly.backend.rsvp.repository;

import com.seatly.backend.rsvp.model.Rsvp;
import com.seatly.backend.rsvp.type.RsvpStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RsvpDao extends JpaRepository<Rsvp, Long> {

    long countByEventIdAndStatus(Long eventId, RsvpStatus status);

    boolean existsByEventIdAndUserIdAndStatus(Long eventId, Long userId, RsvpStatus status);

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
