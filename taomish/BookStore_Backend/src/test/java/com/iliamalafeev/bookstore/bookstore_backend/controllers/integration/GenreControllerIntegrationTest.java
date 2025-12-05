package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Integration tests cannot be run without docker environment for testcontainers.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GenreControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;

    @Autowired
    public GenreControllerIntegrationTest(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @BeforeAll
    static void beforeAll() {

        JdbcDatabaseDelegate containerDelegate = new JdbcDatabaseDelegate(postgre, "");

        ScriptUtils.runInitScript(containerDelegate, "schema.sql");
        ScriptUtils.runInitScript(containerDelegate, "data.sql");
    }

    @Test
    void connectionEstablished() {
        assertTrue(postgre.isCreated());
        assertTrue(postgre.isRunning());
    }

    @Test
    void findAll_shouldReturnAllGenres() {

        String url = "/api/genres";

        ResponseEntity<List<GenreDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});

        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }
}
