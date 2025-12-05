package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.controllers.integration.custom_page_implementation.CustomRestPageResponse;
import com.iliamalafeev.bookstore.bookstore_backend.dto.DiscussionDTO;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.DiscussionRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.DiscussionException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Integration tests cannot be run without docker environment for testcontainers.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DiscussionControllerIntegrationTest {

    private final String baseURL = "/api/discussions/secure";
    private final String paginationParams = "?page=0&discussions-per-page=5";
    private final HttpHeaders headers = new HttpHeaders();

    private static JdbcDatabaseDelegate containerDelegate;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final DiscussionRepository discussionRepository;

    @Autowired
    DiscussionControllerIntegrationTest(TestRestTemplate restTemplate, AuthenticationService authenticationService, DiscussionRepository discussionRepository) {
        this.restTemplate = restTemplate;
        this.authenticationService = authenticationService;
        this.discussionRepository = discussionRepository;
    }

    private String getJwt() {

        PersonLoginDTO personLoginDTO = new PersonLoginDTO("email1@email.com", "userPassword");
        BindingResult bindingResult = new BeanPropertyBindingResult(personLoginDTO, "personLoginDTO");

        AuthenticationResponse authenticationResponse = authenticationService.authenticatePerson(personLoginDTO, bindingResult);
        return authenticationResponse.getToken();
    }

    @BeforeAll
    static void beforeAll() {

        containerDelegate = new JdbcDatabaseDelegate(postgre, "");

        ScriptUtils.runInitScript(containerDelegate, "schema.sql");
        ScriptUtils.runInitScript(containerDelegate, "data.sql");
    }

    @BeforeEach
    void beforeEach() {

        ScriptUtils.runInitScript(containerDelegate, "data.sql");
    }

    @AfterEach
    void afterEach() {

        discussionRepository.deleteAllInBatch();
    }

    @Test
    void findAllByPersonEmail_shouldReturnAllDiscussionsByPersonEmail() {

        String url = baseURL + paginationParams;

        String token = getJwt();

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ParameterizedTypeReference<CustomRestPageResponse<DiscussionDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<DiscussionDTO>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
    }

    @Test
    void findAllByPersonEmail_shouldReturnUnauthorizedIfJwtIsInvalid() {

        String url = baseURL + paginationParams;

        String token = "eyINVALID_TOKEN.eyINVALID_TOKEN.INVALID_TOKEN";

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<MalformedJwtException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MalformedJwtException.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your authentication token is invalid or malformed, please re-login.", response.getBody().getMessage());
    }

    @Test
    void addDiscussion_shouldAddDiscussionToDatabaseAndReturnIt() {

        String url = baseURL + "/add-discussion";

        String token = getJwt();

        DiscussionDTO body = new DiscussionDTO();
        body.setTitle("Title");
        body.setQuestion("Question");

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<DiscussionDTO> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, DiscussionDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());
        assertEquals(body.getTitle(), response.getBody().getTitle());
        assertEquals(body.getQuestion(), response.getBody().getQuestion());
        assertFalse(response.getBody().getClosed());
    }

    @Test
    void addDiscussion_shouldReturnForbiddenIfDiscussionDtoIsInvalid() {

        String url = baseURL + "/add-discussion";

        String token = getJwt();

        DiscussionDTO body = new DiscussionDTO();
        body.setTitle(null);
        body.setQuestion("Question");

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<DiscussionException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, DiscussionException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. title: Discussion title must be present and contain at least 1 character; ", response.getBody().getMessage());
    }
}