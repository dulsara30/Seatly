package com.seatly.backend.rsvp.mapper;

import com.seatly.backend.rsvp.model.Rsvp;
import com.seatly.backend.rsvp.payload.AttendeeResponseDto;
import com.seatly.backend.rsvp.payload.MyRsvpResponseDto;
import com.seatly.backend.rsvp.payload.RsvpResponseDto;
import com.seatly.backend.rsvp.payload.WaitlistEntryResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/** Field-for-field; source paths like "user.name" are navigation, not logic. */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RsvpMapper {

    @Mapping(target = "eventId", source = "event.id")
    RsvpResponseDto toResponseDto(Rsvp rsvp);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "eventDate", source = "event.eventDate")
    @Mapping(target = "eventStatus", source = "event.status")
    MyRsvpResponseDto toMyRsvpResponseDto(Rsvp rsvp);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "rsvpAt", source = "createdAt")
    AttendeeResponseDto toAttendeeResponseDto(Rsvp rsvp);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "rsvpAt", source = "createdAt")
    WaitlistEntryResponseDto toWaitlistEntryResponseDto(Rsvp rsvp);
}
