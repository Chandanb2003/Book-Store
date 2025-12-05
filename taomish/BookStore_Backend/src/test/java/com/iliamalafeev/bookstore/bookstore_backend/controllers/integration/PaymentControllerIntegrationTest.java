package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.dto.PaymentInfoDTO;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PaymentRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PaymentException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
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
public class PaymentControllerIntegrationTest {

    private final String personEmail = "email1@email.com";
    private final String baseURL = "/api/payment/secure";
    private final HttpHeaders headers = new HttpHeaders();

    private static JdbcDatabaseDelegate containerDelegate;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentControllerIntegrationTest(TestRestTemplate restTemplate, AuthenticationService authenticationService, PaymentRepository paymentRepository) {
        this.restTemplate = restTemplate;
        this.authenticationService = authenticationService;
        this.paymentRepository = paymentRepository;
    }

    private String getJwt(String personEmail) {

        PersonLoginDTO personLoginDTO = new PersonLoginDTO(personEmail, "userPassword");
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

        paymentRepository.deleteAllInBatch();
    }

    @Test
    void findByPersonEmail_shouldReturnPaymentFeesForAuthenticatedPerson() {

        String token = getJwt(personEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Double> response = restTemplate.exchange(baseURL, HttpMethod.GET, httpEntity, Double.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10.00, response.getBody());
    }

    @Test
    void findByPersonEmail_shouldReturnUnauthorizedIfJwtIsInvalid() {

        String token = "eyINVALID_TOKEN.eyINVALID_TOKEN.INVALID_TOKEN";

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<MalformedJwtException> response = restTemplate.exchange(baseURL, HttpMethod.GET, httpEntity, MalformedJwtException.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your authentication token is invalid or malformed, please re-login.", response.getBody().getMessage());
    }

    @Test
    void createPaymentIntent_shouldCreateValidStripePaymentIntent() {

        String url = baseURL + "/payment-intent";

        String token = getJwt(personEmail);

        PaymentInfoDTO body = new PaymentInfoDTO();
        body.setAmount(1000);
        body.setCurrency("EUR");
        body.setReceiptEmail(personEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<PaymentInfoDTO> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"amount\": 1000"));
        assertTrue(response.getBody().contains("\"currency\": \"eur\""));
    }

    @Test
    void stripePaymentComplete_shouldUpdatePaymentInfoForAuthenticatedPerson() {

        String url = baseURL + "/payment-complete";

        String token = getJwt(personEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void stripePaymentComplete_shouldReturnNotFoundIfPaymentInfoDoesNotExist() {

        String url = baseURL + "/payment-complete";

        String token = getJwt("email2@email.com");

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<PaymentException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, PaymentException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Payment information is missing", response.getBody().getMessage());
    }
}
