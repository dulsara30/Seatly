package com.seatly.backend;

import com.seatly.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

// Extends the integration base so it uses Testcontainers, not the docker-compose database.
class BackendApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}
