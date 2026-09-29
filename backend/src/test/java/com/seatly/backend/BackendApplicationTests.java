package com.seatly.backend;

import com.seatly.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

// Shares the integration-test context, so it runs against Testcontainers
// Postgres instead of needing the local docker-compose database.
class BackendApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
