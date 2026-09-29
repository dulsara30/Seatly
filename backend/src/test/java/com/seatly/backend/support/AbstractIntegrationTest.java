package com.seatly.backend.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import com.seatly.backend.common.security.JwtTokenService;
import com.seatly.backend.common.type.ResponseStatusType;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.json.JsonMapper;

/**
 * Full stack: HTTP (MockMvc, no port needed) -> security filter chain ->
 * controller -> service -> Postgres. Every subclass shares one application
 * context and one container.
 *
 * Deliberately NOT @Transactional. A test transaction would make every
 * request share one Hibernate session — a GET after a PATCH could read the
 * cached entity instead of the database, and a failed request's rollback
 * would be invisible. Each request here runs in its own real transaction,
 * exactly as in production, and reset() isolates tests instead.
 *
 * Requests go through the real JWT filter with real tokens. The helpers
 * without a suffix act as the seeded organiser — most event tests are about
 * the rules, not about who is calling — and the ...As / ...Anonymously
 * variants make any other caller explicit.
 */
@SpringBootTest(properties = "seatly.jwt.signing-key=" + AbstractIntegrationTest.TEST_SIGNING_KEY)
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, FixedClockConfiguration.class})
public abstract class AbstractIntegrationTest {

    /** Test-only key (32 random bytes, Base64). Never used outside tests; CI needs no secret. */
    public static final String TEST_SIGNING_KEY = "vXu9mWI6wkifjIt+v+OvFWIXj9o1gckQPfFkCP6Lv5s=";

    protected static final String EVENTS_PATH = "/v1/events";
    protected static final String EVENT_PATH = "/v1/events/{eventId}";

    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    protected MockMvcTester mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtTokenService jwtTokenService;

    protected TestData testData;

    @BeforeEach
    void resetDatabase() {
        testData = new TestData(jdbcTemplate);
        testData.reset();
    }

    protected String toJson(Object body) {
        return jsonMapper.writeValueAsString(body);
    }

    /** Reads one value out of a response, for when a test needs it as input to the next request. */
    protected <T> T readJson(MvcTestResult result, String jsonPath) {
        return JsonPath.read(new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8), jsonPath);
    }

    protected String bearerTokenFor(long userId) {
        return BEARER_PREFIX + jwtTokenService.issueAccessToken(userId);
    }

    protected MvcTestResult post(String path, Object body) {
        return postAs(path, body, TestData.SEEDED_ORGANISER_ID);
    }

    protected MvcTestResult postAs(String path, Object body, long userId) {
        return mvc.post().uri(path).header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId))
                .contentType(MediaType.APPLICATION_JSON).content(toJson(body)).exchange();
    }

    protected MvcTestResult postAnonymously(String path, Object body) {
        return mvc.post().uri(path).contentType(MediaType.APPLICATION_JSON).content(toJson(body)).exchange();
    }

    protected MvcTestResult patch(long eventId, Object body) {
        return patchAs(eventId, body, TestData.SEEDED_ORGANISER_ID);
    }

    protected MvcTestResult patchAs(long eventId, Object body, long userId) {
        return mvc.patch().uri(EVENT_PATH, eventId).header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId))
                .contentType(MediaType.APPLICATION_JSON).content(toJson(body)).exchange();
    }

    protected MvcTestResult get(long eventId) {
        return getAs(eventId, TestData.SEEDED_ORGANISER_ID);
    }

    protected MvcTestResult getAs(long eventId, long userId) {
        return mvc.get().uri(EVENT_PATH, eventId).header(HttpHeaders.AUTHORIZATION, bearerTokenFor(userId)).exchange();
    }

    protected MvcTestResult getAnonymously(long eventId) {
        return mvc.get().uri(EVENT_PATH, eventId).exchange();
    }

    /** The error envelope: unsuccessful, exactly one message, the expected key. */
    protected void assertError(MvcTestResult result, HttpStatus status, String messageKey) {
        assertThat(result).hasStatus(status);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo(ResponseStatusType.UNSUCCESSFUL.getValue());
        assertThat(result).bodyJson().extractingPath("$.results").asArray().hasSize(1);
        assertThat(result).bodyJson().extractingPath("$.results[0].message").isEqualTo(messageKey);
    }
}
