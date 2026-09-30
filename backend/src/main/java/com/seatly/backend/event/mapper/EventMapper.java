package com.seatly.backend.event.mapper;

import com.seatly.backend.event.model.Event;
import com.seatly.backend.event.payload.CreateEventRequestDto;
import com.seatly.backend.event.payload.EventDetailResponseDto;
import com.seatly.backend.event.payload.EventResponseDto;
import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.tag.model.Tag;
import com.seatly.backend.tag.payload.TagSummaryDto;
import com.seatly.backend.user.model.User;
import com.seatly.backend.user.payload.UserSummaryDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EventMapper {

    EventResponseDto toResponseDto(Event event, long availableSeats);

    EventDetailResponseDto toDetailResponseDto(Event event, long availableSeats);

    @Mapping(target = "meetingLink", ignore = true)
    EventDetailResponseDto toDetailResponseDtoWithoutMeetingLink(Event event, long availableSeats);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "linkSentAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Event toEntity(CreateEventRequestDto request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "linkSentAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(UpdateEventRequestDto request, @MappingTarget Event event);

    TagSummaryDto toTagSummaryDto(Tag tag);

    UserSummaryDto toUserSummaryDto(User user);
}
