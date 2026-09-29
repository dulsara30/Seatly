package com.seatly.backend.event.repository;

import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.model.Event_;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface EventDao extends JpaRepository<Event, Long>, EventRepository {

    // A single event, so fetching the tag collection eagerly is safe here —
    // the paging problem only exists for lists.
    @EntityGraph(attributePaths = {Event_.ORGANIZER, Event_.TAGS})
    Optional<Event> findByIdAndIsDeletedFalse(Long id);

    /**
     * SELECT ... FOR UPDATE. Used by updates that compare seatLimit against
     * the confirmed count: without the lock, an RSVP could be confirmed
     * between the count and the save, and the new limit would be below it.
     * RSVP creation locks the same row, so the two serialise.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Event> findLockedByIdAndIsDeletedFalse(Long id);
}
