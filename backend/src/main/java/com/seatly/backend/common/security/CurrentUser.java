package com.seatly.backend.common.security;

import com.seatly.backend.common.exception.ForbiddenException;
import com.seatly.backend.common.exception.UnauthorizedException;
import com.seatly.backend.common.type.CommonMessageKey;
import com.seatly.backend.common.type.MessageKey;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Who is making this request — the one place services ask. It replaces the
 * CURRENT_USER_ID placeholder, and its ownership check is shared by every
 * module that has an "only the owner may do this" rule (event organiser now,
 * RSVP next).
 */
@Component
public class CurrentUser {

    /** Empty for anonymous callers — public endpoints are reachable without a token. */
    public Optional<Long> findId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Anonymous requests carry an AnonymousAuthenticationToken whose
        // principal is a plain String, so the type check covers that case too.
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user.id());
    }

    /** For operations that only make sense with a caller, e.g. creating an event. */
    public Long requireId() {
        return findId().orElseThrow(() -> new UnauthorizedException(CommonMessageKey.AUTHENTICATION_REQUIRED));
    }

    public boolean isUser(Long userId) {
        return findId().filter(userId::equals).isPresent();
    }

    /**
     * The shared ownership guard. The caller supplies the key, so each module
     * still reports its own reason — EVENT_ERROR_NOT_ORGANIZER, and so on.
     */
    public void requireOwner(Long ownerId, MessageKey notOwnerKey) {
        if (!isUser(ownerId)) {
            throw new ForbiddenException(notOwnerKey);
        }
    }
}
