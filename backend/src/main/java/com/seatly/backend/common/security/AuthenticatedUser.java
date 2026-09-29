package com.seatly.backend.common.security;

/**
 * The principal stored in the SecurityContext for a request with a valid
 * token. Only the id: the token is the whole session, so nothing else is
 * trusted without a fresh database read.
 */
public record AuthenticatedUser(Long id) {
}
