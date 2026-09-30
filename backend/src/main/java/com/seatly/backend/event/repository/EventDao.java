package com.seatly.backend.event.repository;

import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.model.Event_;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface EventDao extends JpaRepository<Event, Long>, EventRepository {

    // A single event, so fetch-joining the tag collection is safe (the paging problem is lists only).
    @EntityGraph(attributePaths = {Event_.ORGANIZER, Event_.TAGS})
    Optional<Event> findByIdAndIsDeletedFalse(Long id);

    // FOR UPDATE prevents overselling: the seat-limit check is check-then-act.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Event> findLockedByIdAndIsDeletedFalse(Long id);

    List<Event> findByOrganizerIdAndIsDeletedFalseOrderByEventDateAsc(Long organizerId);
}
