package com.seatly.backend.rsvp.service;

import com.seatly.backend.common.exception.ConflictException;
import com.seatly.backend.common.exception.EntityNotFoundException;
import com.seatly.backend.common.exception.ValidationException;
import com.seatly.backend.common.security.CurrentUser;
import com.seatly.backend.common.util.SeatUtils;
import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.repository.EventDao;
import com.seatly.backend.event.service.SeatCountPublisher;
import com.seatly.backend.event.type.EventMessageKey;
import com.seatly.backend.event.type.EventStatus;
import com.seatly.backend.rsvp.mapper.RsvpMapper;
import com.seatly.backend.rsvp.model.Rsvp;
import com.seatly.backend.rsvp.payload.AttendeeListResponseDto;
import com.seatly.backend.rsvp.payload.AttendeeResponseDto;
import com.seatly.backend.rsvp.payload.MyRsvpResponseDto;
import com.seatly.backend.rsvp.payload.RsvpResponseDto;
import com.seatly.backend.rsvp.payload.WaitlistEntryResponseDto;
import com.seatly.backend.rsvp.repository.RsvpDao;
import com.seatly.backend.rsvp.type.RsvpMessageKey;
import com.seatly.backend.rsvp.type.RsvpStatus;
import com.seatly.backend.user.repository.UserDao;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Every writer locks the event row (FOR UPDATE) before rsvp rows: no oversell, no deadlock.
@Service
@RequiredArgsConstructor
public class RsvpServiceImpl implements RsvpService {

    private static final int FIRST_POSITION = 1;

    private final EventDao eventDao;
    private final RsvpDao rsvpDao;
    private final UserDao userDao;
    private final RsvpMapper rsvpMapper;
    private final WaitlistManager waitlistManager;
    private final SeatCountPublisher seatCountPublisher;
    private final CurrentUser currentUser;

    // A last-seat race loser blocks on the lock, then is WAITLISTED, not an error.
    @Override
    @Transactional
    public RsvpResponseDto createRsvp(Long eventId) {
        Long userId = currentUser.requireId();
        Event event = findLockedEventOrThrow(eventId);

        if (currentUser.isUser(event.getOrganizer().getId())) {
            throw new ValidationException(RsvpMessageKey.OWN_EVENT);
        }
        validateIsUpcoming(event);

        // Re-RSVP reuses the cancelled row because of UNIQUE(user_id, event_id).
        Optional<Rsvp> existing = rsvpDao.findByEventIdAndUserId(eventId, userId);
        if (existing.isPresent() && existing.get().getStatus().isActive()) {
            throw new ConflictException(RsvpMessageKey.ALREADY_EXISTS);
        }
        Rsvp rsvp = existing.orElseGet(() -> newRsvp(event, userId));

        long confirmedCount = rsvpDao.countByEventIdAndStatus(eventId, RsvpStatus.CONFIRMED);
        if (SeatUtils.availableSeats(event.getSeatLimit(), confirmedCount) > 0) {
            rsvp.setStatus(RsvpStatus.CONFIRMED);
            rsvp.setPosition(null);
        } else {
            rsvp.setStatus(RsvpStatus.WAITLISTED);
            rsvp.setPosition(nextWaitlistPosition(eventId));
        }

        Rsvp saved = rsvpDao.save(rsvp);
        // Published even when waitlisted, so every viewer still sees an update.
        seatCountPublisher.publishChange(event);
        return rsvpMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public RsvpResponseDto cancelRsvp(Long eventId) {
        Long userId = currentUser.requireId();
        Event event = findLockedEventOrThrow(eventId);
        validateIsUpcoming(event);

        Rsvp rsvp = rsvpDao.findByEventIdAndUserId(eventId, userId)
                .filter(existing -> existing.getStatus().isActive())
                .orElseThrow(() -> new EntityNotFoundException(RsvpMessageKey.NOT_FOUND));

        rsvp.setStatus(RsvpStatus.CANCELLED);
        rsvp.setPosition(null);
        waitlistManager.rebalance(event);
        seatCountPublisher.publishChange(event);

        return rsvpMapper.toResponseDto(rsvp);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendeeListResponseDto getAttendees(Long eventId) {
        Event event = findOrganisersEventOrThrow(eventId);
        List<AttendeeResponseDto> attendees = rsvpDao
                .findWithUserByEventIdAndStatusOrderByCreatedAtAsc(eventId, RsvpStatus.CONFIRMED).stream()
                .map(rsvpMapper::toAttendeeResponseDto)
                .toList();
        return new AttendeeListResponseDto(attendees, attendees.size(), event.getSeatLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistEntryResponseDto> getWaitlist(Long eventId) {
        findOrganisersEventOrThrow(eventId);
        return rsvpDao.findWithUserByEventIdAndStatusOrderByPositionAsc(eventId, RsvpStatus.WAITLISTED).stream()
                .map(rsvpMapper::toWaitlistEntryResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyRsvpResponseDto> getMyRsvps() {
        return rsvpDao.findWithEventByUserIdAndStatusInOrderByEventEventDateAsc(currentUser.requireId(),
                        RsvpStatus.ACTIVE).stream()
                .map(rsvpMapper::toMyRsvpResponseDto)
                .toList();
    }

    private Event findLockedEventOrThrow(Long eventId) {
        return eventDao.findLockedByIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException(EventMessageKey.NOT_FOUND));
    }

    private Event findOrganisersEventOrThrow(Long eventId) {
        Event event = eventDao.findByIdAndIsDeletedFalse(eventId)
                .orElseThrow(() -> new EntityNotFoundException(EventMessageKey.NOT_FOUND));
        currentUser.requireOwner(event.getOrganizer().getId(), EventMessageKey.NOT_ORGANIZER);
        return event;
    }

    private void validateIsUpcoming(Event event) {
        if (event.getStatus() != EventStatus.UPCOMING) {
            throw new ValidationException(RsvpMessageKey.EVENT_NOT_UPCOMING);
        }
    }

    // WaitlistManager keeps the queue contiguous (1..n), so the next place is length + 1.
    private int nextWaitlistPosition(Long eventId) {
        return Math.toIntExact(rsvpDao.countByEventIdAndStatus(eventId, RsvpStatus.WAITLISTED)) + FIRST_POSITION;
    }

    // getReferenceById avoids a SELECT: only the user's FK is needed.
    private Rsvp newRsvp(Event event, Long userId) {
        Rsvp rsvp = new Rsvp();
        rsvp.setEvent(event);
        rsvp.setUser(userDao.getReferenceById(userId));
        return rsvp;
    }
}
