package com.seatly.backend.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.seatly.backend.common.type.ResponseStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.json.JsonMapper;

/**
 * Full stack: HTTP (MockMvc, no port needed) -> controller -> service ->
 * Postgres. Every subclass shares one application context and one container.
 *
 * Deliberately NOT @Transactional. A test transaction would make every
 * request share one Hibernate session — a GET after a PATCH could read the
 * cached entity instead of the database, and a failed request's rollback
 * would be invisible. Each request here runs in its own real transaction,
 * exactly as in production, and reset() isolates tests instead.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, FixedClockConfiguration.class})
public abstract class AbstractIntegrationTest {

    protected static final String EVENTS_PATH = "/v1/events";
    protected static final String EVENT_PATH = "/v1/events/{eventId}";

    @Autowired
    protected MockMvcTester mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    protected TestData testData;

    @BeforeEach
    void resetDatabase() {
        testData = new TestData(jdbcTemplate);
        testData.reset();
    }

    protected String toJson(Object body) {
        return jsonMapper.writeValueAsString(body);
    }

    protected MvcTestResult post(String path, Object body) {
        return mvc.post().uri(path).contentType(MediaType.APPLICATION_JSON).content(toJson(body)).exchange();
    }

    protected MvcTestResult patch(long eventId, Object body) {
        return mvc.patch().uri(EVENT_PATH, eventId).contentType(MediaType.APPLICATION_JSON).content(toJson(body))
                .exchange();
    }

    protected MvcTestResult get(long eventId) {
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
