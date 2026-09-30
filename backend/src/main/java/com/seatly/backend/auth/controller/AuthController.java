package com.seatly.backend.auth.controller;

import com.seatly.backend.auth.payload.AuthResponseDto;
import com.seatly.backend.auth.payload.LoginRequestDto;
import com.seatly.backend.auth.payload.RegisterRequestDto;
import com.seatly.backend.auth.service.AuthService;
import com.seatly.backend.common.constant.ApiPaths;
import com.seatly.backend.common.constant.ApiResponseCodes;
import com.seatly.backend.common.payload.ResponseEntityDto;
import com.seatly.backend.user.payload.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, log in, and read your own account")
public class AuthController {

    private final AuthService authService;

    // Empty @SecurityRequirements: these endpoints issue the token, so they can't require one.
    @PostMapping(ApiPaths.AUTH_REGISTER)
    @SecurityRequirements
    @Operation(summary = "Register an account")
    @ApiResponse(responseCode = ApiResponseCodes.CREATED, description = "Account created")
    @ApiResponse(responseCode = ApiResponseCodes.BAD_REQUEST, description = "Invalid name, email, password or bio")
    @ApiResponse(responseCode = ApiResponseCodes.CONFLICT, description = "Email already registered")
    public ResponseEntity<ResponseEntityDto<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseEntityDto.success(authService.register(request)));
    }

    @PostMapping(ApiPaths.AUTH_LOGIN)
    @SecurityRequirements
    @Operation(summary = "Log in", description = "Returns an access token. Paste it into Swagger's Authorize button.")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "Logged in")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Wrong email or password")
    public ResponseEntity<ResponseEntityDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(ResponseEntityDto.success(authService.login(request)));
    }

    @GetMapping(ApiPaths.AUTH_ME)
    @Operation(summary = "Get my account")
    @ApiResponse(responseCode = ApiResponseCodes.OK, description = "The caller's account")
    @ApiResponse(responseCode = ApiResponseCodes.UNAUTHORIZED, description = "Missing, invalid or expired token")
    public ResponseEntity<ResponseEntityDto<UserResponseDto>> getCurrentUser() {
        return ResponseEntity.ok(ResponseEntityDto.success(authService.getCurrentUser()));
    }
}
