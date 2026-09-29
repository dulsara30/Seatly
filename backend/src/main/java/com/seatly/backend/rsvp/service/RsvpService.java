package com.seatly.backend.rsvp.service;

import com.seatly.backend.rsvp.payload.AttendeeListResponseDto;
import com.seatly.backend.rsvp.payload.MyRsvpResponseDto;
import com.seatly.backend.rsvp.payload.RsvpResponseDto;
import com.seatly.backend.rsvp.payload.WaitlistEntryResponseDto;
import java.util.List;

public interface RsvpService {

    RsvpResponseDto createRsvp(Long eventId);

    RsvpResponseDto cancelRsvp(Long eventId);

    AttendeeListResponseDto getAttendees(Long eventId);

    List<WaitlistEntryResponseDto> getWaitlist(Long eventId);

    List<MyRsvpResponseDto> getMyRsvps();
}
