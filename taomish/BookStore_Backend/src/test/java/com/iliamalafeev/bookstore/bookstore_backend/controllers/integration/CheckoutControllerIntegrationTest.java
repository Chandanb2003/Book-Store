package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.dto.CheckoutDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled("Integration tests cannot be run without docker environment for testcontainers.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CheckoutControllerIntegrationTest {

    private final String baseURL = "/api/checkouts/secure";
    private final HttpHeaders headers = new HttpHeaders();

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;
    private final AuthenticationService authenticationService;

    @Autowired
    CheckoutControllerIntegrationTest(TestRestTemplate restTemplate, AuthenticationService authenticationService) {
        this.restTemplate = restTemplate;
        this.authenticationService = authenticationService;
    }

    private String getJwt() {

        PersonLoginDTO personLoginDTO = new PersonLoginDTO("email1@email.com", "userPassword");
        BindingResult bindingResult = new BeanPropertyBindingResult(personLoginDTO, "personLoginDTO");

        AuthenticationResponse authenticationResponse = authenticationService.authenticatePerson(personLoginDTO, bindingResult);
        return authenticationResponse.getToken();
    }

    @BeforeAll
    static void beforeAll() {

        JdbcDatabaseDelegate containerDelegate = new JdbcDatabaseDelegate(postgre, "");

        ScriptUtils.runInitScript(containerDelegate, "schema.sql");
        ScriptUtils.runInitScript(containerDelegate, "data.sql");
    }

    @Test
    void getCurrentCheckoutsCount_shouldReturnCountOfCheckoutsMadeByPerson() {

        String url = baseURL + "/current-loans-count";

        String token = getJwt();

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Integer> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Integer.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody());
    }

    @Test
    void getCurrentCheckoutsCount_shouldReturnUnauthorizedIfJwtIsInvalid() {

        String url = baseURL + "/current-loans-count";

        String token = "eyINVALID_TOKEN.eyINVALID_TOKEN.INVALID_TOKEN";

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<MalformedJwtException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MalformedJwtException.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your authentication token is invalid or malformed, please re-login.", response.getBody().getMessage());
    }

    @Test
    void getCurrentCheckouts_shouldReturnAllCurrentCheckoutsMadeByPerson() {

        String url = baseURL + "/current-checkouts";

        String token = getJwt();

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ParameterizedTypeReference<List<CheckoutDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<List<CheckoutDTO>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getCurrentCheckouts_shouldReturnUnauthorizedIfJwtIsInvalid() {

        String url = baseURL + "/current-checkouts";

        String token = "eyINVALID_TOKEN.eyINVALID_TOKEN.INVALID_TOKEN";

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<MalformedJwtException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MalformedJwtException.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your authentication token is invalid or malformed, please re-login.", response.getBody().getMessage());
    }
}