package com.seatly.backend.event.controller;

import com.seatly.backend.common.constant.ApiPaths;
import com.seatly.backend.common.constant.ApiResponseCodes;
import com.seatly.backend.common.constant.PaginationConstants;
import com.seatly.backend.common.payload.PageDto;
import com.seatly.backend.common.payload.ResponseEntityDto;
import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.event.payload.CreateEventRequestDto;
import com.seatly.backend.event.payload.EventDetailResponseDto;
import com.seatly.backend.event.payload.EventFilterDto;
import com.seatly.backend.event.payload.EventResponseDto;
import com.seatly.backend.event.payload.UpdateEventRequestDto;
import com.seatly.backend.event.service.EventService;
import com.seatly.backend.event.type.EventMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.EVENTS)
@RequiredArgsConstructor
@Tag(name = "Events", description = "Create, browse and edit events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create an event", description = "The caller becomes the event's organiser.")
    @ApiResponse(responseCode = ApiResponseCodes.CREATED, description = "Event created")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST,
            description = "Invalid field, date not in the future, venue missing for the mode, or unknown tag id")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    public ResponseEntity<ResponseEntityDto<EventDetailResponseDto>> createEvent(
            @Valid @RequestBody CreateEventRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEntityDto.success(eventService.createEvent(request)));
    }

    @GetMapping
    @Operation(summary = "List upcoming events",
            description = "Soonest first. tag, mode and search are optional and combine with AND.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "A page of upcoming events")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST, description = "Page or size out of range, or unknown mode")
    public ResponseEntity<ResponseEntityDto<PageDto<EventResponseDto>>> getUpcomingEvents(
            @RequestParam(defaultValue = PaginationConstants.FIRST_PAGE)
            @Min(value = PaginationConstants.MIN_PAGE_NUMBER, message = CommonMessageKeys.PAGE_NUMBER_INVALID)
            int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE)
            @Min(value = PaginationConstants.MIN_PAGE_SIZE, message = CommonMessageKeys.PAGE_SIZE_INVALID)
            @Max(value = PaginationConstants.MAX_PAGE_SIZE, message = CommonMessageKeys.PAGE_SIZE_INVALID)
            int size,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) EventMode mode,
            @RequestParam(required = false) String search) {
        EventFilterDto filter = new EventFilterDto(tag, mode, search);
        return ResponseEntity.ok(ResponseEntityDto.success(eventService.getUpcomingEvents(filter, page, size)));
    }

    // A literal segment beats {eventId} in Spring's matching, so "/my" is never parsed as an id.
    @GetMapping("/my")
    @Operation(summary = "List my events", description = "Events the caller organises, every status, soonest first.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "The caller's events")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    public ResponseEntity<ResponseEntityDto<EventResponseDto>> getMyEvents() {
        return ResponseEntity.ok(ResponseEntityDto.success(eventService.getMyEvents()));
    }

    @PostMapping("/{eventId}/cancel")
    @Operation(summary = "Cancel an event",
            description = "Organiser only. Irreversible: the event becomes CANCELLED and stops accepting RSVPs.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "Event cancelled")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST, description = "Event is already cancelled or completed")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    @ApiResponse(responseCode = ApiResponseCodes.FORBIDDEN, description = "Caller is not the organiser")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event, or it was deleted")
    public ResponseEntity<ResponseEntityDto<EventDetailResponseDto>> cancelEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(ResponseEntityDto.success(eventService.cancelEvent(eventId)));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get an event",
            description = "Public. meetingLink is included only when the token belongs to the organiser "
                    + "or a confirmed attendee.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "The event")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event, or it was deleted")
    public ResponseEntity<ResponseEntityDto<EventDetailResponseDto>> getEventById(@PathVariable Long eventId) {
        return ResponseEntity.ok(ResponseEntityDto.success(eventService.getEventById(eventId)));
    }

    @PatchMapping("/{eventId}")
    @Operation(summary = "Update an event",
            description = "Partial update: omitted fields are unchanged. Switching mode clears the other venue field.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "Event updated")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST,
            description = "Invalid field, event not UPCOMING, seat limit below confirmed count, or unknown tag id")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    @ApiResponse(responseCode = ApiResponseCodes.FORBIDDEN, description = "Caller is not the organiser")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event, or it was deleted")
    public ResponseEntity<ResponseEntityDto<EventDetailResponseDto>> updateEvent(
            @PathVariable Long eventId, @Valid @RequestBody UpdateEventRequestDto request) {
        return ResponseEntity.ok(ResponseEntityDto.success(eventService.updateEvent(eventId, request)));
    }
}
