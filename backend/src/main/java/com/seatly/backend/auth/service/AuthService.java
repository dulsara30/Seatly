package com.seatly.backend.auth.service;

import com.seatly.backend.auth.payload.AuthResponseDto;
import com.seatly.backend.auth.payload.LoginRequestDto;
import com.seatly.backend.auth.payload.RegisterRequestDto;
import com.seatly.backend.user.payload.UserResponseDto;

public interface AuthService {

    UserResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    UserResponseDto getCurrentUser();
}
