package com.seatly.backend.auth;

import static com.seatly.backend.support.FixedClockConfiguration.FIXED_INSTANT;
import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.auth.payload.LoginRequestDto;
import com.seatly.backend.auth.payload.RegisterRequestDto;
import com.seatly.backend.auth.type.AuthMessageKeys;
import com.seatly.backend.common.type.CommonMessageKeys;
import com.seatly.backend.support.AbstractIntegrationTest;
import com.seatly.backend.support.TestData;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class AuthIntegrationTest extends AbstractIntegrationTest {

    private static final String REGISTER_PATH = "/v1/auth/register";
    private static final String LOGIN_PATH = "/v1/auth/login";
    private static final String ME_PATH = "/v1/auth/me";

    private static final String PASSWORD = "correct horse battery";
    private static final String OUR_ISSUER = "seatly";

    // ---- register ----

    @Test
    void registersUserWithoutEverReturningThePassword() {
        MvcTestResult result = postAnonymously(REGISTER_PATH, registration("new@seatly.test"));

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.results[0].email").isEqualTo("new@seatly.test");
        assertThat(result).bodyJson().doesNotHavePath("$.results[0].password");
    }

    @Test
    void storesPasswordAsBcryptHashNotPlainText() {
        postAnonymously(REGISTER_PATH, registration("hashed@seatly.test"));

        assertThat(testData.passwordHashOf("hashed@seatly.test")).startsWith("$2a$").isNotEqualTo(PASSWORD);
    }

    @Test
    void storesEmailInLowercase() {
        MvcTestResult result = postAnonymously(REGISTER_PATH, registration("Mixed.Case@Seatly.Test"));

        assertThat(result).bodyJson().extractingPath("$.results[0].email").isEqualTo("mixed.case@seatly.test");
    }

    @Test
    void rejectsEmailAlreadyRegistered() {
        assertError(postAnonymously(REGISTER_PATH, registration(TestData.SEEDED_ATTENDEE_EMAIL)),
                HttpStatus.CONFLICT, AuthMessageKeys.EMAIL_ALREADY_REGISTERED);
    }

    // Case doesn't make it a different inbox, so it can't be a second account.
    @Test
    void rejectsEmailAlreadyRegisteredInDifferentCase() {
        assertError(postAnonymously(REGISTER_PATH, registration("ATTENDEE@SEATLY.DEV")),
                HttpStatus.CONFLICT, AuthMessageKeys.EMAIL_ALREADY_REGISTERED);
    }

    @Test
    void rejectsInvalidEmail() {
        assertError(postAnonymously(REGISTER_PATH, registration("not-an-email")),
                HttpStatus.BAD_REQUEST, AuthMessageKeys.EMAIL_INVALID);
    }

    @Test
    void rejectsPasswordShorterThanMinimum() {
        RegisterRequestDto request = new RegisterRequestDto("New User", "short@seatly.test", "1234567", null);

        assertError(postAnonymously(REGISTER_PATH, request), HttpStatus.BAD_REQUEST, AuthMessageKeys.PASSWORD_TOO_SHORT);
    }

    // 30 characters, but 90 bytes in UTF-8 — over BCrypt's 72-byte limit even
    // though a character count would call it fine.
    @Test
    void rejectsPasswordOverBcryptByteLimit() {
        String multiBytePassword = "日".repeat(30);
        RegisterRequestDto request = new RegisterRequestDto("New User", "long@seatly.test", multiBytePassword, null);

        assertError(postAnonymously(REGISTER_PATH, request), HttpStatus.BAD_REQUEST, AuthMessageKeys.PASSWORD_TOO_LONG);
    }

    // The global trimmer must not touch passwords: surrounding spaces are part
    // of what the user typed, so only the exact string logs in.
    @Test
    void keepsSurroundingSpacesInPassword() {
        String spacedPassword = "  " + PASSWORD + "  ";
        postAnonymously(REGISTER_PATH, new RegisterRequestDto("New User", "spaces@seatly.test", spacedPassword, null));

        assertThat(postAnonymously(LOGIN_PATH, new LoginRequestDto("spaces@seatly.test", spacedPassword)))
                .hasStatusOk();
        assertError(postAnonymously(LOGIN_PATH, new LoginRequestDto("spaces@seatly.test", PASSWORD)),
                HttpStatus.UNAUTHORIZED, AuthMessageKeys.INVALID_CREDENTIALS);
    }

    // ---- login ----

    @Test
    void logsInSeededAccountAndReturnsUsableToken() {
        MvcTestResult login = postAnonymously(LOGIN_PATH,
                new LoginRequestDto(TestData.SEEDED_ATTENDEE_EMAIL, TestData.SEEDED_PASSWORD));

        assertThat(login).hasStatusOk();
        String token = readJson(login, "$.results[0].accessToken");
        MvcTestResult me = meWithToken(token);
        assertThat(me).hasStatusOk();
        assertThat(me).bodyJson().extractingPath("$.results[0].email").isEqualTo(TestData.SEEDED_ATTENDEE_EMAIL);
    }

    @Test
    void logsInWithEmailInAnyCase() {
        assertThat(postAnonymously(LOGIN_PATH,
                new LoginRequestDto("Attendee@Seatly.Dev", TestData.SEEDED_PASSWORD))).hasStatusOk();
    }

    @Test
    void rejectsWrongPassword() {
        assertError(postAnonymously(LOGIN_PATH, new LoginRequestDto(TestData.SEEDED_ATTENDEE_EMAIL, "wrong-password")),
                HttpStatus.UNAUTHORIZED, AuthMessageKeys.INVALID_CREDENTIALS);
    }

    // Same key as a wrong password: login must not reveal which emails exist.
    @Test
    void rejectsUnknownEmailWithTheSameError() {
        assertError(postAnonymously(LOGIN_PATH, new LoginRequestDto("nobody@seatly.test", TestData.SEEDED_PASSWORD)),
                HttpStatus.UNAUTHORIZED, AuthMessageKeys.INVALID_CREDENTIALS);
    }

    @Test
    void rejectsDeletedAccount() {
        testData.softDeleteUser(testData.userIdByEmail(TestData.SEEDED_ATTENDEE_EMAIL));

        assertError(postAnonymously(LOGIN_PATH,
                        new LoginRequestDto(TestData.SEEDED_ATTENDEE_EMAIL, TestData.SEEDED_PASSWORD)),
                HttpStatus.UNAUTHORIZED, AuthMessageKeys.INVALID_CREDENTIALS);
    }

    @Test
    void rejectsDeactivatedAccount() {
        testData.deactivateUser(testData.userIdByEmail(TestData.SEEDED_ATTENDEE_EMAIL));

        assertError(postAnonymously(LOGIN_PATH,
                        new LoginRequestDto(TestData.SEEDED_ATTENDEE_EMAIL, TestData.SEEDED_PASSWORD)),
                HttpStatus.UNAUTHORIZED, AuthMessageKeys.INVALID_CREDENTIALS);
    }

    // ---- me, and every way a token can be wrong ----

    @Test
    void returnsTheCallersOwnAccount() {
        MvcTestResult result = mvc.get().uri(ME_PATH)
                .header(HttpHeaders.AUTHORIZATION, bearerTokenFor(TestData.SEEDED_ORGANISER_ID)).exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.results[0].email").isEqualTo(TestData.SEEDED_ORGANISER_EMAIL);
    }

    @Test
    void rejectsMissingToken() {
        assertError(mvc.get().uri(ME_PATH).exchange(), HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    @Test
    void rejectsMalformedToken() {
        assertError(meWithToken("not-a-jwt"), HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    // Expiry is checked against the injected Clock — frozen here, so "an hour
    // ago" is exact and the test can't flake around a boundary.
    @Test
    void rejectsExpiredToken() {
        String expired = token(testSigningKey(), OUR_ISSUER, FIXED_INSTANT.minus(Duration.ofSeconds(1)));

        assertError(meWithToken(expired), HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    // Well-formed and unexpired, but signed with someone else's key: a forgery.
    @Test
    void rejectsTokenSignedWithAnotherKey() {
        String forged = token(Jwts.SIG.HS256.key().build(), OUR_ISSUER, FIXED_INSTANT.plus(Duration.ofHours(1)));

        assertError(meWithToken(forged), HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    @Test
    void rejectsTokenFromAnotherIssuer() {
        String foreign = token(testSigningKey(), "someone-else", FIXED_INSTANT.plus(Duration.ofHours(1)));

        assertError(meWithToken(foreign), HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    // The token outlives nothing about the account.
    @Test
    void rejectsValidTokenOfDeletedAccount() {
        long attendeeId = testData.userIdByEmail(TestData.SEEDED_ATTENDEE_EMAIL);
        String token = bearerTokenFor(attendeeId);
        testData.softDeleteUser(attendeeId);

        assertError(mvc.get().uri(ME_PATH).header(HttpHeaders.AUTHORIZATION, token).exchange(),
                HttpStatus.UNAUTHORIZED, CommonMessageKeys.AUTHENTICATION_REQUIRED);
    }

    private RegisterRequestDto registration(String email) {
        return new RegisterRequestDto("New User", email, PASSWORD, "Hello");
    }

    private MvcTestResult meWithToken(String rawToken) {
        return mvc.get().uri(ME_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken).exchange();
    }

    private SecretKey testSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SIGNING_KEY));
    }

    private String token(SecretKey key, String issuer, Instant expiresAt) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(TestData.SEEDED_ORGANISER_ID))
                .issuedAt(Date.from(FIXED_INSTANT.minus(Duration.ofHours(1))))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }
}
