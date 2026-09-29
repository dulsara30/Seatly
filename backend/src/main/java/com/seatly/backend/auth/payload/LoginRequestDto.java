package com.seatly.backend.auth.payload;

import com.seatly.backend.auth.type.AuthMessageKeys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * No format or length rules beyond "present": a login attempt must never
 * reveal which rules a real password follows. Anything wrong is simply
 * INVALID_CREDENTIALS.
 */
public record LoginRequestDto(

        @NotBlank(message = AuthMessageKeys.EMAIL_REQUIRED)
        String email,

        // Untrimmed, matching registration — see RegisterRequestDto.
        @JsonDeserialize(using = StringDeserializer.class)
        @NotEmpty(message = AuthMessageKeys.PASSWORD_REQUIRED)
        String password) {
}
