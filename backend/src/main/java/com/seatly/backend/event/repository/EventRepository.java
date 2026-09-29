package com.seatly.backend.event.repository;

import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.payload.EventFilterDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom fragment for queries whose shape depends on the input. Spring Data
 * finds the implementation by naming convention: EventRepositoryImpl.
 */
public interface EventRepository {

    Page<Event> findUpcoming(EventFilterDto filter, Pageable pageable);
}
