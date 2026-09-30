package com.seatly.backend.auth.payload;

import com.seatly.backend.auth.type.AuthMessageKeys;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

// No format rules: a login attempt must not reveal which rules a real password follows.
public record LoginRequestDto(

        @NotBlank(message = AuthMessageKeys.EMAIL_REQUIRED)
        String email,

        // Untrimmed, matching RegisterRequestDto.
        @JsonDeserialize(using = StringDeserializer.class)
        @NotEmpty(message = AuthMessageKeys.PASSWORD_REQUIRED)
        String password) {
}
