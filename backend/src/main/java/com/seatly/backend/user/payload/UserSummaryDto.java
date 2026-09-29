package com.seatly.backend.user.payload;

/**
 * The public face of a user when embedded in another resource — never email,
 * password or any other account detail.
 */
public record UserSummaryDto(Long id, String name) {
}
