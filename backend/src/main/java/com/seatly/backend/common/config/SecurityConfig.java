package com.seatly.backend.common.config;

import com.seatly.backend.common.constant.ApiPaths;
import com.seatly.backend.common.security.JsonAuthenticationEntryPoint;
import com.seatly.backend.common.security.JwtAuthenticationFilter;
import com.seatly.backend.common.security.JwtProperties;
import com.seatly.backend.common.security.JwtTokenService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless: no session, no cookie, the bearer token is the whole identity —
 * which is also why CSRF protection is off (CSRF abuses cookies the browser
 * sends automatically; a bearer header is never sent automatically).
 *
 * Rules are evaluated top to bottom; anything not listed needs a token.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    // Spring Boot forwards unhandled errors to /error. If that path needed a
    // token, a failure on a public endpoint would be reported as a 401.
    private static final String ERROR_PATH = "/error";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenService jwtTokenService,
            JsonAuthenticationEntryPoint authenticationEntryPoint) {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, ApiPaths.AUTH_REGISTER, ApiPaths.AUTH_LOGIN).permitAll()
                        // Must come before the public rule below: /v1/events/my is
                        // one path segment too, and the first matching rule wins.
                        .requestMatchers(HttpMethod.GET, ApiPaths.EVENTS_MY).authenticated()
                        .requestMatchers(HttpMethod.GET, ApiPaths.EVENTS, ApiPaths.EVENTS_SINGLE_SEGMENT).permitAll()
                        // Public like the event page: counts only, no personal data.
                        .requestMatchers(HttpMethod.GET, ApiPaths.EVENT_STREAM).permitAll()
                        .requestMatchers(SWAGGER_PATHS).permitAll()
                        .requestMatchers(ERROR_PATH).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
