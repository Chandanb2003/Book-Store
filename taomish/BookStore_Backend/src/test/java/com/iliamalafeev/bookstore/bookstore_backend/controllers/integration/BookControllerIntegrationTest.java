package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.controllers.integration.custom_page_implementation.CustomRestPageResponse;
import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.BookRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.CheckoutRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.HistoryRecordRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PaymentRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.BookException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.GenreException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PaymentException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.ReviewException;
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

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Integration tests cannot be run without docker environment for testcontainers.")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerIntegrationTest {

    private final String baseURL = "/api/books";
    private final String paginationParams = "?page=0&books-per-page=5";
    private final Long validBookId = 10000001L;
    private final String invalidBookId = "10100101";
    private final String firstUserEmail = "email1@email.com";
    private final String secondUserEmail = "email2@email.com";
    private final String thirdUserEmail = "email3@email.com";
    private final HttpHeaders headers = new HttpHeaders();

    private static JdbcDatabaseDelegate containerDelegate;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final BookRepository bookRepository;
    private final CheckoutRepository checkoutRepository;
    private final PaymentRepository paymentRepository;
    private final HistoryRecordRepository historyRecordRepository;

    @Autowired
    BookControllerIntegrationTest(TestRestTemplate restTemplate, AuthenticationService authenticationService, BookRepository bookRepository, CheckoutRepository checkoutRepository, PaymentRepository paymentRepository, HistoryRecordRepository historyRecordRepository) {
        this.restTemplate = restTemplate;
        this.authenticationService = authenticationService;
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
        this.paymentRepository = paymentRepository;
        this.historyRecordRepository = historyRecordRepository;
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

        bookRepository.deleteAllInBatch();
        checkoutRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        historyRecordRepository.deleteAllInBatch();
    }

    @Test
    void connectionEstablished() {
        assertTrue(postgre.isCreated());
        assertTrue(postgre.isRunning());
    }

    @Test
    void findAll_shouldReturnAllBooksPaginated() {

        String url = baseURL + paginationParams;

        ParameterizedTypeReference<CustomRestPageResponse<BookDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<BookDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(5, response.getBody().getTotalElements());
    }

    @Test
    void findById_shouldReturnBookDtoById() {

        String url = baseURL + "/" + validBookId;

        ResponseEntity<BookDTO> response = restTemplate.exchange(url, HttpMethod.GET, null, BookDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(validBookId, response.getBody().getId());
        assertEquals("Title 1", response.getBody().getTitle());
        assertEquals("Author 1", response.getBody().getAuthor());
        assertEquals("Description 1", response.getBody().getDescription());
        assertEquals(10, response.getBody().getCopies());
        assertEquals(10, response.getBody().getCopiesAvailable());
        assertEquals("Encoded image 1", response.getBody().getImg());
    }

    @Test
    void findById_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/" + invalidBookId;

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.GET, null, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void findAllByTitle_shouldReturnAllBooksByTitlePaginated() {

        String url = baseURL + "/search/by-title" + paginationParams + "&title-query=title";

        ParameterizedTypeReference<CustomRestPageResponse<BookDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<BookDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(5, response.getBody().getTotalElements());
    }

    @Test
    void findAllByGenre_shouldReturnAllBooksByGenrePaginated() {

        String url = baseURL + "/search/by-genre" + paginationParams + "&genre-query=Genre 2";

        ParameterizedTypeReference<CustomRestPageResponse<BookDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<BookDTO>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(3, response.getBody().getTotalElements());
    }

    @Test
    void findAllByGenre_shouldReturnNotFoundIfGenreIsIncorrect() {

        String url = baseURL + "/search/by-genre" + paginationParams + "&genre-query=IncorrectGenre";

        ResponseEntity<GenreException> response = restTemplate.exchange(url, HttpMethod.GET, null, GenreException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No such genre found ", response.getBody().getMessage());
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnTrueIfBookIsCheckedOutByPerson() {

        String url = baseURL + "/secure/is-checked-out/10000002";

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Boolean.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnFalseIfBookIsNotCheckedOutByPerson() {

        String url = baseURL + "/secure/is-checked-out/" + validBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Boolean.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody());
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnUnauthorizedIfJwtIsInvalid() {

        String url = baseURL + "/secure/is-checked-out/" + validBookId;

        String token = "eyINVALID_TOKEN.eyINVALID_TOKEN.INVALID_TOKEN";

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<MalformedJwtException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MalformedJwtException.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your authentication token is invalid or malformed, please re-login.", response.getBody().getMessage());
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/secure/is-checked-out/" + invalidBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void checkoutBook_shouldCreateCheckoutEntityAndUpdateBook() {

        String url = baseURL + "/secure/checkout/" + validBookId;

        String token = getJwt(secondUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void checkoutBook_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/secure/checkout/" + invalidBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void checkoutBook_shouldReturnForbiddenIfCopiesOrCopiesAvailableIsAlreadyZero() {

        String url = baseURL + "/secure/checkout/10000005";

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book quantity is already 0 ", response.getBody().getMessage());
    }

    @Test
    void checkoutBook_shouldReturnForbiddenIfCheckoutAlreadyExists() {

        String url = baseURL + "/secure/checkout/10000002";

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book is already checked out by this user ", response.getBody().getMessage());
    }

    @Test
    void checkoutBook_shouldReturnForbiddenIfPaymentIsGreaterThanZeroOrSomeBooksAreOverdue() {

        String url = baseURL + "/secure/checkout/" + validBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<PaymentException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, PaymentException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("You have outstanding fees / overdue books, checkout is unavailable", response.getBody().getMessage());
    }

    @Test
    void renewCheckout_shouldUpdateCheckoutEntityAndUpdateBook() {

        String url = baseURL + "/secure/renew-checkout/10000004";

        String token = getJwt(secondUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void renewCheckout_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/secure/renew-checkout/" + invalidBookId;

        String token = getJwt(secondUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void renewCheckout_shouldReturnForbiddenIfBookIsNotCheckedOutByUser() {

        String url = baseURL + "/secure/renew-checkout/" + validBookId;

        String token = getJwt(secondUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("This book is not checked out by this user ", response.getBody().getMessage());
    }

    @Test
    void renewCheckout_shouldReturnForbiddenIfBookIsOverdue() {

        String url = baseURL + "/secure/renew-checkout/10000004";

        String token = getJwt(thirdUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("This book is overdue ", response.getBody().getMessage());
    }

    @Test
    void returnBook_shouldDeleteCheckoutEntityAndUpdateBookAndCreateHistoryRecordEntity() {

        String url = baseURL + "/secure/return/10000002";

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void returnBook_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/secure/return/" + invalidBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void returnBook_shouldReturnForbiddenIfBookIsNotCheckedOutByUser() {

        String url = baseURL + "/secure/return/" + validBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("This book is not checked out by this user ", response.getBody().getMessage());
    }

    @Test
    void returnBook_shouldReturnNotFoundIfPaymentInfoIsNotFound() {

        String url = baseURL + "/secure/return/10000004";

        String token = getJwt(thirdUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<PaymentException> response = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, PaymentException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Payment information is missing", response.getBody().getMessage());
    }

    @Test
    void isBookReviewedByPerson_shouldReturnTrueIfBookIsReviewedByPerson() {

        String url = baseURL + "/secure/is-reviewed/" + validBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Boolean.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
    }

    @Test
    void isBookReviewedByPerson_shouldReturnFalseIfBookIsNotReviewedByPerson() {

        String url = baseURL + "/secure/is-reviewed/10000002";

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<Boolean> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Boolean.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody());
    }

    @Test
    void isBookReviewedByPerson_shouldReturnUnauthorizedIfJwtIsInvalid() {

        String url = baseURL + "/secure/is-reviewed/" + validBookId;

        String token = "eyINVALID_TOKEN.eyINVALID_TOKEN.INVALID_TOKEN";

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<MalformedJwtException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, MalformedJwtException.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Your authentication token is invalid or malformed, please re-login.", response.getBody().getMessage());
    }

    @Test
    void isBookReviewedByPerson_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/secure/is-reviewed/" + invalidBookId;

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void reviewBook_shouldCreateReviewEntityAndReturnSavedReviewDTO() {

        String url = baseURL + "/secure/review/" + validBookId;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(4.5);
        reviewDTO.setReviewDescription("Description");
        reviewDTO.setDate(LocalDateTime.now());

        String token = getJwt(thirdUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<ReviewDTO> httpEntity = new HttpEntity<>(reviewDTO, headers);

        ResponseEntity<ReviewDTO> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ReviewDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(thirdUserEmail, response.getBody().getPersonEmail());
        assertEquals("First Name 3", response.getBody().getPersonFirstName());
        assertEquals(reviewDTO.getReviewDescription(), response.getBody().getReviewDescription());
        assertEquals(reviewDTO.getRating(), response.getBody().getRating());
    }

    @Test
    void reviewBook_shouldReturnForbiddenIfReviewDtoIsInvalid() {

        String url = baseURL + "/secure/review/" + validBookId;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(null);
        reviewDTO.setReviewDescription("Description");
        reviewDTO.setDate(LocalDateTime.now());

        String token = getJwt(thirdUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<ReviewDTO> httpEntity = new HttpEntity<>(reviewDTO, headers);

        ResponseEntity<ReviewException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ReviewException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. rating: Rating must be present; rating: Rating must be at least 0.5; ", response.getBody().getMessage());
    }

    @Test
    void reviewBook_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/secure/review/" + invalidBookId;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(4.5);
        reviewDTO.setReviewDescription("Description");
        reviewDTO.setDate(LocalDateTime.now());

        String token = getJwt(thirdUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<ReviewDTO> httpEntity = new HttpEntity<>(reviewDTO, headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void reviewBook_shouldReturnForbiddenIfBookIsAlreadyReviewedByPerson() {

        String url = baseURL + "/secure/review/" + validBookId;

        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setRating(4.5);
        reviewDTO.setReviewDescription("Description");
        reviewDTO.setDate(LocalDateTime.now());

        String token = getJwt(firstUserEmail);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<ReviewDTO> httpEntity = new HttpEntity<>(reviewDTO, headers);

        ResponseEntity<ReviewException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, ReviewException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("This book is already reviewed by this person ", response.getBody().getMessage());
    }
}