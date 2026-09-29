package com.seatly.backend.event.service;

import com.seatly.backend.common.exception.EntityNotFoundException;
import com.seatly.backend.common.exception.ValidationException;
import com.seatly.backend.common.payload.PageDto;
import com.seatly.backend.common.security.CurrentUser;
import com.seatly.backend.event.mapper.EventMapper;
import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.model.Event_;
import com.seatly.backend.event.payload.CreateEventRequestDto;
import com.seatly.backend.event.payload.EventDetailResponseDto;
import com.seatly.backend.event.payload.EventFilterDto;
import com.seatly.backend.event.payload.EventResponseDto;
import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.event.repository.EventDao;
import com.seatly.backend.event.type.EventMessageKey;
import com.seatly.backend.event.type.EventMode;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.repository.EventRsvpCount;
import com.seatly.backend.rsvp.repository.RsvpDao;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.tag.model.Tag;
import com.seatly.backend.tag.repository.TagDao;
import com.seatly.backend.user.repository.UserDao;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final long NO_CONFIRMED_RSVPS = 0L;

    // Soonest first — what someone browsing for an event to attend wants.
    private static final Sort UPCOMING_EVENTS_ORDER = Sort.by(Sort.Direction.ASC, Event_.EVENT_DATE);

    private final EventDao eventDao;
    private final RsvpDao rsvpDao;
    private final TagDao tagDao;
    private final UserDao userDao;
    private final EventMapper eventMapper;
    private final CurrentUser currentUser;
    private final Clock clock;

    @Override
    @Transactional
    public EventDetailResponseDto createEvent(CreateEventRequestDto request) {
        validateEventDateInFuture(request.eventDate());
        validateVenueForMode(request.mode(), request.location(), request.meetingLink());
        Set<Tag> tags = findTagsOrThrow(request.tagIds());

        Event event = eventMapper.toEntity(request);
        // A reference, not a load: the organizer is only needed as a foreign
        // key here, so there is no reason to SELECT the whole user row.
        event.setOrganizer(userDao.getReferenceById(currentUser.requireId()));
        event.setStatus(EventStatus.UPCOMING);
        event.setTags(tags);
        clearVenueFieldUnusedByMode(event);

        return toDetailResponse(eventDao.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDto<EventResponseDto> getUpcomingEvents(EventFilterDto filter, int page, int size) {
        Page<Event> events = eventDao.findUpcoming(
                normalizeTagFilter(filter), PageRequest.of(page, size, UPCOMING_EVENTS_ORDER));
        Map<Long, Long> confirmedCountByEventId = countConfirmedPerEvent(events.getContent());

        List<EventResponseDto> items = events.getContent().stream()
                .map(event -> eventMapper.toResponseDto(
                        event, availableSeats(event, confirmedCountOf(event, confirmedCountByEventId))))
                .toList();

        return new PageDto<>(items, events.getNumber(), events.getTotalPages(), events.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public EventDetailResponseDto getEventById(Long eventId) {
        Event event = eventDao.findByIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException(EventMessageKey.NOT_FOUND));
        return toDetailResponse(event);
    }

    /**
     * Order matters: who (403) before state (400) before content (400), so a
     * non-organiser learns nothing about the event's internals. Every query
     * runs before the entity is modified — a query after a modification would
     * make Hibernate flush the half-validated change to the database first.
     */
    @Override
    @Transactional
    public EventDetailResponseDto updateEvent(Long eventId, UpdateEventRequestDto request) {
        // FOR UPDATE: see EventDao — the seat-limit check is check-then-act.
        Event event = eventDao.findLockedByIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException(EventMessageKey.NOT_FOUND));

        validateCallerIsOrganizer(event);
        validateIsUpcoming(event);
        validateUpdatedFields(event, request);
        applyTagChange(event, request.tagIds());

        eventMapper.updateEntity(request, event);
        // Validated on the merged state: a PATCH may change mode alone, or the
        // venue field alone, and only the combination can be right or wrong.
        // Throwing here rolls the transaction back, discarding the change.
        validateVenueForMode(event.getMode(), event.getLocation(), event.getMeetingLink());
        clearVenueFieldUnusedByMode(event);

        return toDetailResponse(event);
    }

    private void validateUpdatedFields(Event event, UpdateEventRequestDto request) {
        if (request.eventDate() != null) {
            validateEventDateInFuture(request.eventDate());
        }
        if (request.seatLimit() != null) {
            validateSeatLimitCoversConfirmed(event, request.seatLimit());
        }
    }

    private void applyTagChange(Event event, Set<Long> tagIds) {
        // null = the client did not send tags, so they stay as they are.
        // An empty set is different: it removes every tag.
        if (tagIds != null) {
            event.setTags(findTagsOrThrow(tagIds));
        }
    }

    private void validateEventDateInFuture(LocalDateTime eventDate) {
        if (!eventDate.isAfter(LocalDateTime.now(clock))) {
            throw new ValidationException(EventMessageKey.DATE_MUST_BE_FUTURE);
        }
    }

    private void validateVenueForMode(EventMode mode, String location, String meetingLink) {
        if (mode == EventMode.ONLINE && meetingLink == null) {
            throw new ValidationException(EventMessageKey.MEETING_LINK_REQUIRED);
        }
        if (mode == EventMode.PHYSICAL && location == null) {
            throw new ValidationException(EventMessageKey.LOCATION_REQUIRED);
        }
    }

    // Symmetric: whichever venue field the mode doesn't use is cleared, so a
    // PHYSICAL -> ONLINE switch can't leave a stale address behind, and an
    // ONLINE -> PHYSICAL switch can't leave a stale meeting link.
    private void clearVenueFieldUnusedByMode(Event event) {
        switch (event.getMode()) {
            case ONLINE -> event.setLocation(null);
            case PHYSICAL -> event.setMeetingLink(null);
        }
    }

    private void validateCallerIsOrganizer(Event event) {
        currentUser.requireOwner(organizerIdOf(event), EventMessageKey.NOT_ORGANIZER);
    }

    private void validateIsUpcoming(Event event) {
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new ValidationException(EventMessageKey.NOT_UPCOMING);
        }
    }

    // Raising the limit is always allowed. Lowering it below the number of
    // people already confirmed would mean un-confirming someone, so it's refused.
    private void validateSeatLimitCoversConfirmed(Event event, int newSeatLimit) {
        if (newSeatLimit < countConfirmed(event.getId())) {
            throw new ValidationException(EventMessageKey.SEAT_LIMIT_BELOW_CONFIRMED);
        }
    }

    private Set<Tag> findTagsOrThrow(Set<Long> tagIds) {
        List<Tag> tags = tagDao.findAllById(tagIds);
        // tagIds is a Set, so it has no duplicates: any shortfall is an id
        // that does not exist.
        if (tags.size() != tagIds.size()) {
            throw new ValidationException(EventMessageKey.TAG_NOT_FOUND);
        }
        return new HashSet<>(tags);
    }

    private EventFilterDto normalizeTagFilter(EventFilterDto filter) {
        if (filter.tag() == null) {
            return filter;
        }
        // Tags are stored lowercase, so "Tech" must find "tech".
        return new EventFilterDto(filter.tag().toLowerCase(Locale.ROOT), filter.mode(), filter.search());
    }

    private EventDetailResponseDto toDetailResponse(Event event) {
        long availableSeats = availableSeats(event, countConfirmed(event.getId()));
        if (canSeeMeetingLink(event)) {
            return eventMapper.toDetailResponseDto(event, availableSeats);
        }
        return eventMapper.toDetailResponseDtoWithoutMeetingLink(event, availableSeats);
    }

    // A live check on every read, not a one-time grant: cancel your RSVP and
    // the link disappears from your next response.
    // Anonymous visitors can read the event (it's a public endpoint) but
    // never the link.
    private boolean canSeeMeetingLink(Event event) {
        Optional<Long> callerId = currentUser.findId();
        if (callerId.isEmpty()) {
            return false;
        }
        return currentUser.isUser(organizerIdOf(event))
                || rsvpDao.existsByEventIdAndUserIdAndStatus(event.getId(), callerId.get(), RsvpStatus.CONFIRMED);
    }

    // getId() on a lazy proxy returns the foreign key without loading the user.
    private Long organizerIdOf(Event event) {
        return event.getOrganizer().getId();
    }

    private long countConfirmed(Long eventId) {
        return rsvpDao.countByEventIdAndStatus(eventId, RsvpStatus.CONFIRMED);
    }

    // One grouped query for the whole page — never one COUNT per event.
    private Map<Long, Long> countConfirmedPerEvent(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        return rsvpDao.countPerEvent(eventIds, RsvpStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(EventRsvpCount::eventId, EventRsvpCount::rsvpCount));
    }

    // An event missing from the grouped result genuinely has zero confirmed
    // RSVPs — GROUP BY only returns groups that exist. This zero is the real
    // count, not a default standing in for missing data.
    private long confirmedCountOf(Event event, Map<Long, Long> confirmedCountByEventId) {
        return confirmedCountByEventId.getOrDefault(event.getId(), NO_CONFIRMED_RSVPS);
    }

    private long availableSeats(Event event, long confirmedCount) {
        return event.getSeatLimit() - confirmedCount;
    }
}
