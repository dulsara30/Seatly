package com.seatly.backend.rsvp.controller;

import com.seatly.backend.common.constant.ApiPaths;
import com.seatly.backend.common.constant.ApiResponseCodes;
import com.seatly.backend.common.payload.ResponseEntityDto;
import com.seatly.backend.rsvp.payload.AttendeeListResponseDto;
import com.seatly.backend.rsvp.payload.MyRsvpResponseDto;
import com.seatly.backend.rsvp.payload.RsvpResponseDto;
import com.seatly.backend.rsvp.payload.WaitlistEntryResponseDto;
import com.seatly.backend.rsvp.service.RsvpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "RSVP", description = "Reserve a seat, join the waitlist, and manage attendance")
public class RsvpController {

    private final RsvpService rsvpService;

    @PostMapping(ApiPaths.EVENT_RSVP)
    @Operation(summary = "RSVP to an event",
            description = "CONFIRMED if a seat is free, otherwise WAITLISTED with a position. "
                    + "Concurrent RSVPs for the last seat are serialised: one is confirmed, the rest waitlisted.")
    @ApiResponse(responseCode = ApiResponseCodes.CREATED, description = "Confirmed or waitlisted")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST, description = "Own event, or event not UPCOMING")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event")
    @ApiResponse(responseCode = ApiResponseCodes.CONFLICT, description = "Already confirmed or waitlisted")
    public ResponseEntity<ResponseEntityDto<RsvpResponseDto>> createRsvp(@PathVariable Long eventId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseEntityDto.success(rsvpService.createRsvp(eventId)));
    }

    @DeleteMapping(ApiPaths.EVENT_RSVP)
    @Operation(summary = "Cancel my RSVP",
            description = "A freed seat goes to the first person on the waitlist; the queue closes up.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "Cancelled")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST, description = "Event not UPCOMING")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event, or no active RSVP on it")
    public ResponseEntity<ResponseEntityDto<RsvpResponseDto>> cancelRsvp(@PathVariable Long eventId) {
        return ResponseEntity.ok(ResponseEntityDto.success(rsvpService.cancelRsvp(eventId)));
    }

    @GetMapping(ApiPaths.EVENT_ATTENDEES)
    @Operation(summary = "List confirmed attendees", description = "Organiser only.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "Confirmed attendees, earliest RSVP first")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    @ApiResponse(responseCode = ApiResponseCodes.FORBIDDEN, description = "Caller is not the organiser")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event")
    public ResponseEntity<ResponseEntityDto<AttendeeListResponseDto>> getAttendees(@PathVariable Long eventId) {
        return ResponseEntity.ok(ResponseEntityDto.success(rsvpService.getAttendees(eventId)));
    }

    @GetMapping(ApiPaths.EVENT_WAITLIST)
    @Operation(summary = "List the waitlist", description = "Organiser only. Ordered by position.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "The queue, position 1 first")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    @ApiResponse(responseCode = ApiResponseCodes.FORBIDDEN, description = "Caller is not the organiser")
    @ApiResponse(responseCode = ApiResponseCodes.NOT_FOUND, description = "No such event")
    public ResponseEntity<ResponseEntityDto<WaitlistEntryResponseDto>> getWaitlist(@PathVariable Long eventId) {
        return ResponseEntity.ok(ResponseEntityDto.success(rsvpService.getWaitlist(eventId)));
    }

    @GetMapping(ApiPaths.MY_RSVPS)
    @Operation(summary = "List my RSVPs", description = "Active only — confirmed and waitlisted — soonest event first.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "The caller's active RSVPs")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    public ResponseEntity<ResponseEntityDto<MyRsvpResponseDto>> getMyRsvps() {
        return ResponseEntity.ok(ResponseEntityDto.success(rsvpService.getMyRsvps()));
    }
}
