package com.seatly.backend.auth.service;

import com.seatly.backend.auth.payload.AuthResponseDto;
import com.seatly.backend.auth.payload.LoginRequestDto;
import com.seatly.backend.auth.payload.RegisterRequestDto;
import com.seatly.backend.auth.type.AuthMessageKey;
import com.seatly.backend.common.exception.ConflictException;
import com.seatly.backend.common.exception.UnauthorizedException;
import com.seatly.backend.common.exception.ValidationException;
import com.seatly.backend.common.security.CurrentUser;
import com.seatly.backend.common.security.JwtTokenService;
import com.seatly.backend.common.type.CommonMessageKey;
import com.seatly.backend.user.mapper.UserMapper;
import com.seatly.backend.user.model.User;
import com.seatly.backend.user.model.UserFieldLimits;
import com.seatly.backend.user.payload.UserResponseDto;
import com.seatly.backend.user.repository.UserDao;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final CurrentUser currentUser;

    // Dummy hash checked for unknown emails, so login timing can't reveal registered emails.
    private final String unknownUserPasswordHash;

    public AuthServiceImpl(UserDao userDao, UserMapper userMapper, PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService, CurrentUser currentUser) {
        this.userDao = userDao;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.currentUser = currentUser;
        this.unknownUserPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        validatePasswordFitsBcrypt(request.password());
        String email = normalizeEmail(request.email());
        // Friendly check only; the unique constraint on app_user.email is the real guarantee.
        if (userDao.existsByEmail(email)) {
            throw new ConflictException(AuthMessageKey.EMAIL_ALREADY_REGISTERED);
        }

        User user = userMapper.toEntity(request);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));

        return userMapper.toResponseDto(userDao.save(user));
    }

    // One error for every failure, so login can't reveal who has an account.
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        Optional<User> user = userDao.findByEmailAndIsDeletedFalse(normalizeEmail(request.email()));
        if (user.isEmpty()) {
            passwordEncoder.matches(request.password(), unknownUserPasswordHash);
            throw invalidCredentials();
        }
        if (!passwordEncoder.matches(request.password(), user.get().getPassword()) || !user.get().isActive()) {
            throw invalidCredentials();
        }

        return new AuthResponseDto(
                jwtTokenService.issueAccessToken(user.get().getId()), userMapper.toResponseDto(user.get()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser() {
        return userDao.findByIdAndIsDeletedFalse(currentUser.requireId())
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new UnauthorizedException(CommonMessageKey.AUTHENTICATION_REQUIRED));
    }

    private void validatePasswordFitsBcrypt(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > UserFieldLimits.PASSWORD_MAX_BYTES) {
            throw new ValidationException(AuthMessageKey.PASSWORD_TOO_LONG);
        }
    }

    // Same inbox whatever the case, so emails are stored lowercase.
    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT);
    }

    private UnauthorizedException invalidCredentials() {
        return new UnauthorizedException(AuthMessageKey.INVALID_CREDENTIALS);
    }
}
