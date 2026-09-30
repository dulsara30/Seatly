package com.seatly.backend.common.security;

import com.seatly.backend.common.exception.ForbiddenException;
import com.seatly.backend.common.exception.UnauthorizedException;
import com.seatly.backend.common.type.CommonMessageKey;
import com.seatly.backend.common.type.MessageKey;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Optional<Long> findId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Anonymous requests have a String principal, so the type check covers them too.
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user.id());
    }

    public Long requireId() {
        return findId().orElseThrow(() -> new UnauthorizedException(CommonMessageKey.AUTHENTICATION_REQUIRED));
    }

    public boolean isUser(Long userId) {
        return findId().filter(userId::equals).isPresent();
    }

    public void requireOwner(Long ownerId, MessageKey notOwnerKey) {
        if (!isUser(ownerId)) {
            throw new ForbiddenException(notOwnerKey);
        }
    }
}
