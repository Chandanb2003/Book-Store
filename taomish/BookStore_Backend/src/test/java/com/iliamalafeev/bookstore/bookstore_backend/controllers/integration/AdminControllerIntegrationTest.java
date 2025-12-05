package com.iliamalafeev.bookstore.bookstore_backend.controllers.integration;

import com.iliamalafeev.bookstore.bookstore_backend.controllers.integration.custom_page_implementation.CustomRestPageResponse;
import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.DiscussionDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.BookRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.DiscussionRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.BookException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.DiscussionException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
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
class AdminControllerIntegrationTest {

    private final String baseURL = "/api/admin/secure";
    private final String paginationParams = "?page=0&discussions-per-page=5";
    private final Long validBookId = 10000001L;
    private final String invalidBookId = "10100101";
    private final String adminEmail = "admin@email.com";
    private final String adminPassword = "adminpassword";
    private final String userEmail = "email1@email.com";
    private final String userPassword = "userPassword";
    private final HttpHeaders headers = new HttpHeaders();

    private static JdbcDatabaseDelegate containerDelegate;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private final TestRestTemplate restTemplate;
    private final AuthenticationService authenticationService;
    private final BookRepository bookRepository;
    private final DiscussionRepository discussionRepository;

    @Autowired
    AdminControllerIntegrationTest(TestRestTemplate restTemplate, AuthenticationService authenticationService, BookRepository bookRepository, DiscussionRepository discussionRepository) {
        this.restTemplate = restTemplate;
        this.authenticationService = authenticationService;
        this.bookRepository = bookRepository;
        this.discussionRepository = discussionRepository;
    }

    private String getJwt(String username, String password) {

        PersonLoginDTO personLoginDTO = new PersonLoginDTO(username, password);
        BindingResult bindingResult = new BeanPropertyBindingResult(personLoginDTO, "personLoginDTO");

        AuthenticationResponse authenticationResponse = authenticationService.authenticatePerson(personLoginDTO, bindingResult);
        return authenticationResponse.getToken();
    }

    private BookDTO createBookDTO() {

        GenreDTO genreDTO1 = new GenreDTO();
        genreDTO1.setDescription("Genre 1");

        GenreDTO genreDTO2 = new GenreDTO();
        genreDTO2.setDescription("Genre 2");

        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Title");
        bookDTO.setAuthor("Author");
        bookDTO.setDescription("Description");
        bookDTO.setCopies(10);
        bookDTO.setCopiesAvailable(10);
        bookDTO.setImg("Encoded image");
        bookDTO.setGenres(List.of(genreDTO1, genreDTO2));

        return bookDTO;
    }

    private DiscussionDTO createDiscussionDTO() {

        DiscussionDTO discussionDTO = new DiscussionDTO();
        discussionDTO.setId(10000001L);
        discussionDTO.setTitle("Title 1");
        discussionDTO.setQuestion("Question 1");
        discussionDTO.setPersonFirstName("First Name 1");
        discussionDTO.setPersonLastName("Last Name 1");
        discussionDTO.setPersonEmail(userEmail);
        discussionDTO.setAdminEmail(null);
        discussionDTO.setResponse("Response");
        discussionDTO.setClosed(false);

        return discussionDTO;
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
        discussionRepository.deleteAllInBatch();
    }

    @Test
    void connectionEstablished() {
        assertTrue(postgre.isCreated());
        assertTrue(postgre.isRunning());
    }

    @Test
    void postBook_shouldAddNewBookAndReturnSavedBookDTO() {

        String url = baseURL + "/add-book";

        BookDTO requestBody = createBookDTO();

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<BookDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<BookDTO> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, BookDTO.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(requestBody.getTitle(), response.getBody().getTitle());
        assertEquals(requestBody.getAuthor(), response.getBody().getAuthor());
        assertEquals(requestBody.getDescription(), response.getBody().getDescription());
        assertEquals(requestBody.getCopies(), response.getBody().getCopies());
        assertEquals(requestBody.getCopiesAvailable(), response.getBody().getCopiesAvailable());
        assertEquals(requestBody.getImg(), response.getBody().getImg());
    }

    @Test
    void postBook_shouldReturnForbiddenIfUserIsNotAdmin() {

        String url = baseURL + "/add-book";

        BookDTO requestBody = createBookDTO();

        String token = getJwt(userEmail, userPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<BookDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<AccessDeniedException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, AccessDeniedException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void postBook_shouldReturnForbiddenIfBookDtoIsInvalid() {

        String url = baseURL + "/add-book";

        BookDTO requestBody = createBookDTO();
        requestBody.setTitle(null);

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<BookDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. title: Book title must be present and contain at least 1 character; ", response.getBody().getMessage());
    }

    @Test
    void postBook_shouldReturnNotFoundIfGenresAreInvalid() {

        String url = baseURL + "/add-book";

        GenreDTO genreDTO1 = new GenreDTO();
        genreDTO1.setDescription("Invalid genre");

        BookDTO requestBody = createBookDTO();
        requestBody.setGenres(List.of(genreDTO1));

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<BookDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("No such genres found ", response.getBody().getMessage());
    }

    @Test
    void increaseBookQuantity_shouldIncreaseBookQuantity() {

        String url = baseURL + "/increase-quantity/" + validBookId;

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void increaseBookQuantity_shouldReturnForbiddenIfUserIsNotAdmin() {

        String url = baseURL + "/increase-quantity/" + validBookId;

        String token = getJwt(userEmail, userPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<AccessDeniedException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, AccessDeniedException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void increaseBookQuantity_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/increase-quantity/" + invalidBookId;

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void decreaseBookQuantity_shouldDecreaseBookQuantity() {

        String url = baseURL + "/decrease-quantity/" + validBookId;

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void decreaseBookQuantity_shouldReturnForbiddenIfUserIsNotAdmin() {

        String url = baseURL + "/decrease-quantity/" + validBookId;

        String token = getJwt(userEmail, userPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<AccessDeniedException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, AccessDeniedException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void decreaseBookQuantity_shouldReturnNotFoundIfBookIdIsInvalid() {

        String url = baseURL + "/decrease-quantity/" + invalidBookId;

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, BookException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book not found ", response.getBody().getMessage());
    }

    @Test
    void decreaseBookQuantity_shouldReturnForbiddenIfCopiesOrCopiesAvailableIsAlreadyZero() {

        String url = baseURL + "/decrease-quantity/10000005";

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<BookException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, BookException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Book quantity is already 0 ", response.getBody().getMessage());
    }

    @Test
    void deleteBook_shouldDeleteBookById() {

        String url = baseURL + "/delete-book/" + validBookId;

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteBook_shouldReturnForbiddenIfUserIsNotAdmin() {

        String url = baseURL + "/delete-book/" + validBookId;

        String token = getJwt(userEmail, userPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<AccessDeniedException> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, AccessDeniedException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void findAllUnclosedDiscussions_shouldReturnAllOpenDiscussions() {

        String url = baseURL + "/open-discussions" + paginationParams;

        String token = getJwt(adminEmail, adminPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ParameterizedTypeReference<CustomRestPageResponse<DiscussionDTO>> responseType = new ParameterizedTypeReference<>() {};

        ResponseEntity<CustomRestPageResponse<DiscussionDTO>> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, responseType);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertFalse(response.getBody().getContent().get(0).getClosed());
        assertEquals(10000001L, response.getBody().getContent().get(0).getId());
    }

    @Test
    void findAllUnclosedDiscussions_shouldReturnForbiddenIfUserIsNotAdmin() {

        String url = baseURL + "/open-discussions" + paginationParams;

        String token = getJwt(userEmail, userPassword);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<AccessDeniedException> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, AccessDeniedException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateDiscussion_shouldUpdateDiscussionAndSetClosed() {

        String url = baseURL + "/close-discussion";

        String token = getJwt(adminEmail, adminPassword);

        DiscussionDTO requestBody = createDiscussionDTO();

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, String.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfUserIsNotAdmin() {

        String url = baseURL + "/close-discussion";

        String token = getJwt(userEmail, userPassword);

        DiscussionDTO requestBody = createDiscussionDTO();

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<AccessDeniedException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, AccessDeniedException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfDiscussionDtoIsInvalid() {

        String url = baseURL + "/close-discussion";

        String token = getJwt(adminEmail, adminPassword);

        DiscussionDTO requestBody = createDiscussionDTO();
        requestBody.setTitle(null);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<DiscussionException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, DiscussionException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Some fields are invalid. title: Discussion title must be present and contain at least 1 character; ", response.getBody().getMessage());
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfDiscussionDtoDoesNotHaveResponse() {

        String url = baseURL + "/close-discussion";

        String token = getJwt(adminEmail, adminPassword);

        DiscussionDTO requestBody = createDiscussionDTO();
        requestBody.setResponse(null);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<DiscussionException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, DiscussionException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Discussion cannot be closed without administration response ", response.getBody().getMessage());
    }

    @Test
    void updateDiscussion_shouldReturnNotFoundIfDiscussionDoesNotExist() {

        String url = baseURL + "/close-discussion";

        String token = getJwt(adminEmail, adminPassword);

        DiscussionDTO requestBody = createDiscussionDTO();
        requestBody.setId(10100101L);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<DiscussionException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, DiscussionException.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Discussion not found. ", response.getBody().getMessage());
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfDiscussionIsAlreadyClosed() {

        String url = baseURL + "/close-discussion";

        String token = getJwt(adminEmail, adminPassword);

        DiscussionDTO requestBody = createDiscussionDTO();
        requestBody.setId(10000002L);

        headers.add("Authorization", "Bearer " + token);
        HttpEntity<DiscussionDTO> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<DiscussionException> response = restTemplate.exchange(url, HttpMethod.PATCH, httpEntity, DiscussionException.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("This discussion is already closed. ", response.getBody().getMessage());
    }
}