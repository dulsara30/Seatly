package com.seatly.backend.user.payload;

/**
 * The caller's own account — includes email, unlike UserSummaryDto, which is
 * what other people see. Never the password hash.
 */
public record UserResponseDto(Long id, String name, String email, String bio) {
}
