package com.iliamalafeev.bookstore.bookstore_backend.security.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonRegistrationDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Integration tests cannot be run without docker environment for testcontainers.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerIntegrationTest {

    private final String baseURL = "/api/auth";

    private PersonRegistrationDTO personRegistrationDTO;
    private PersonLoginDTO personLoginDTO;

    private static JdbcDatabaseDelegate containerDelegate;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;
    private final PersonRepository personRepository;

    @Autowired
    AuthenticationControllerIntegrationTest(TestRestTemplate restTemplate, PersonRepository personRepository) {
        this.restTemplate = restTemplate;
        this.personRepository = personRepository;
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

        personRegistrationDTO = new PersonRegistrationDTO();
        personRegistrationDTO.setEmail("newEmail@email.com");
        personRegistrationDTO.setPassword("testPassword");
        personRegistrationDTO.setFirstName("First Name");
        personRegistrationDTO.setLastName("Last Name");
        personRegistrationDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));

        personLoginDTO = new PersonLoginDTO();
        personLoginDTO.setEmail("email1@email.com");
        personLoginDTO.setPassword("userPassword");
    }

    @AfterEach
    void afterEach() {

        personRepository.deleteAllInBatch();
    }

    @Test
    void register_shouldRegisterPersonAndReturnAuthenticationResponseWithJWT() {

        String url = baseURL + "/register";

        HttpEntity<PersonRegistrationDTO> httpEntity = new HttpEntity<>(personRegistrationDTO);

        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, AuthenticationResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void register_shouldReturnForbiddenIfPersonRegistrationDtoIsInvalid() {

        String url = baseURL + "/register";

        personRegistrationDTO.setEmail("invalidEmail.com");

        HttpEntity<PersonRegistrationDTO> httpEntity = new HttpEntity<>(personRegistrationDTO);

        ResponseEntity<PersonException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PersonException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. email: This field must be formatted as Email address; ", response.getBody().getMessage());
    }

    @Test
    void register_shouldReturnForbiddenIfEmailAlreadyRegistered() {

        String url = baseURL + "/register";

        personRegistrationDTO.setEmail("email1@email.com");

        HttpEntity<PersonRegistrationDTO> httpEntity = new HttpEntity<>(personRegistrationDTO);

        ResponseEntity<PersonException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PersonException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. email: Person with this email is already registered; ", response.getBody().getMessage());
    }

    @Test
    void authenticate_shouldCheckCredentialsAndReturnAuthenticationResponseWithJWT() {

        String url = baseURL + "/authenticate";

        HttpEntity<PersonLoginDTO> httpEntity = new HttpEntity<>(personLoginDTO);

        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void authenticate_shouldReturnForbiddenIfPersonLoginDtoIsInvalid() {

        String url = baseURL + "/authenticate";

        personLoginDTO.setEmail("invalidEmail.com");

        HttpEntity<PersonLoginDTO> httpEntity = new HttpEntity<>(personLoginDTO);

        ResponseEntity<PersonException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PersonException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. email: This field must be formatted as Email address; ", response.getBody().getMessage());
    }

    @Test
    void authenticate_shouldReturnForbiddenIfCredentialsAreInvalid() {

        String url = baseURL + "/authenticate";

        personLoginDTO.setPassword("invalidPassword");

        HttpEntity<PersonLoginDTO> httpEntity = new HttpEntity<>(personLoginDTO);

        ResponseEntity<PersonException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PersonException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login or password is incorrect. ", response.getBody().getMessage());
    }

    @Test
    void authenticate_shouldReturnForbiddenIfPersonWithSuchEmailDoesNotExist() {

        String url = baseURL + "/authenticate";

        personLoginDTO.setEmail("newEmail@email.com");

        HttpEntity<PersonLoginDTO> httpEntity = new HttpEntity<>(personLoginDTO);

        ResponseEntity<PersonException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PersonException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login or password is incorrect. ", response.getBody().getMessage());
    }
}