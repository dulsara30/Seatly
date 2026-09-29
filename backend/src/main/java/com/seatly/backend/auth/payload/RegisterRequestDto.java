package com.seatly.backend.auth.payload;

import com.seatly.backend.auth.type.AuthMessageKeys;
import com.seatly.backend.user.model.UserFieldLimits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

public record RegisterRequestDto(

        @NotBlank(message = AuthMessageKeys.NAME_REQUIRED)
        @Size(max = UserFieldLimits.NAME_MAX_LENGTH, message = AuthMessageKeys.NAME_TOO_LONG)
        String name,

        @NotBlank(message = AuthMessageKeys.EMAIL_REQUIRED)
        @Email(message = AuthMessageKeys.EMAIL_INVALID)
        @Size(max = UserFieldLimits.EMAIL_MAX_LENGTH, message = AuthMessageKeys.EMAIL_TOO_LONG)
        String email,

        // Opted out of the global trimmer: spaces are part of a password. If
        // "  secret  " were silently stored as "secret", the password the
        // user chose would not be the one that works. @NotEmpty, not
        // @NotBlank, for the same reason.
        @JsonDeserialize(using = StringDeserializer.class)
        @NotEmpty(message = AuthMessageKeys.PASSWORD_REQUIRED)
        @Size(min = UserFieldLimits.PASSWORD_MIN_LENGTH, message = AuthMessageKeys.PASSWORD_TOO_SHORT)
        String password,

        @Size(max = UserFieldLimits.BIO_MAX_LENGTH, message = AuthMessageKeys.BIO_TOO_LONG)
        String bio) {
}
