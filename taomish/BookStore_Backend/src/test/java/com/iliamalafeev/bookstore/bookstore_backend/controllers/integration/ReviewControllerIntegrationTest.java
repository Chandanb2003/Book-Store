package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.controllers.integration.custom_page_implementation.CustomRestPageResponse;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.BookException;
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

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Integration tests cannot be run without docker environment for testcontainers.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReviewControllerIntegrationTest {

    private final Long bookId = 10000001L;
    private final Long invalidBookId = 11000001L;
    private final String baseURL = "/api/reviews/";
    private final String paginationParams = "?page=0&reviews-per-page=5";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;

    @Autowired
    public ReviewControllerIntegrationTest(TestRestTemplate restTemplate) {
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
    void findAllByBookId_shouldReturnAllReviewsByBookIdPaginated() {

        String url = baseURL + bookId + paginationParams;

        ParameterizedTypeReference<CustomRestPageResponse<ReviewDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<ReviewDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void findAllByBookId_shouldReturnAllReviewsByBookIdPaginatedAndSorted() {

        String url = baseURL + bookId + paginationParams + "&latest=true";

        ParameterizedTypeReference<CustomRestPageResponse<ReviewDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<ReviewDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    void findAllByBookId_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + invalidBookId + paginationParams;

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.GET, null, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void getAverageRatingByBookId_shouldReturnAverageRatingOfAllBookReviewsByBookId() {

        String url = baseURL + "average-rating/" + bookId;

        ResponseEntity<Double> response  = restTemplate.exchange(url, HttpMethod.GET, null, Double.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(4.0, response.getBody());
    }

    @Test
    void getAverageRatingByBookId_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "average-rating/" + invalidBookId;

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.GET, null, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }
}
