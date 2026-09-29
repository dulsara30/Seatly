package com.seatly.backend.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Turns a valid "Authorization: Bearer <token>" header into an authenticated
 * request. It never rejects anything itself: with no header or a bad token the
 * request simply continues unauthenticated, public endpoints still work, and
 * SecurityConfig's rules turn it into a 401 wherever a login is required.
 *
 * Deliberately not a @Component. Spring Boot registers every Filter bean as a
 * servlet filter too, which would run this twice — once outside the security
 * chain. SecurityConfig creates it and places it inside the chain only.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            jwtTokenService.parseUserId(header.substring(BEARER_PREFIX.length())).ifPresent(this::authenticate);
        }
        chain.doFilter(request, response);
    }

    // A fresh context rather than mutating the shared one — the recommended
    // pattern since Spring Security 5.7, and nothing leaks between requests.
    private void authenticate(Long userId) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                new AuthenticatedUser(userId), null, List.of()));
        SecurityContextHolder.setContext(context);
    }
}
