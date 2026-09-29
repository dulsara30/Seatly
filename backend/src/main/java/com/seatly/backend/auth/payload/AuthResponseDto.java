package com.seatly.backend.auth.payload;

import com.seatly.backend.user.payload.UserResponseDto;

public record AuthResponseDto(String accessToken, UserResponseDto user) {
}
