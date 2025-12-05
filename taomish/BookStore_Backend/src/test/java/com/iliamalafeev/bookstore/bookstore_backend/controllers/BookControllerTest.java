package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.BookService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@WithMockUser
class BookControllerTest {

    private final Long bookId = 1L;
    private final Long invalidBookId = 1000L;
    private final String jwtToken = "TestJWT";
    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";
    private final int page = 0;
    private final int booksPerPage = 5;
    private final String baseURL = "/api/books";

    private BookDTO bookDTO1;
    private BookDTO bookDTO2;
    private ReviewDTO reviewDTO;
    private ReviewDTO savedReviewDTO;

    @MockBean private BookService bookService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    BookControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        GenreDTO genreDTO1 = new GenreDTO();
        genreDTO1.setDescription("Genre 1");

        bookDTO1 = new BookDTO();
        bookDTO1.setId(1L);
        bookDTO1.setTitle("Title 1");
        bookDTO1.setAuthor("Author 1");
        bookDTO1.setDescription("Description 1");
        bookDTO1.setCopies(10);
        bookDTO1.setCopiesAvailable(10);
        bookDTO1.setImg("encodedImage 1");
        bookDTO1.setGenres(List.of(genreDTO1));

        bookDTO2 = new BookDTO();
        bookDTO2.setId(2L);
        bookDTO2.setTitle("Title 2");
        bookDTO2.setAuthor("Author 2");
        bookDTO2.setDescription("Description 2");
        bookDTO2.setCopies(10);
        bookDTO2.setCopiesAvailable(10);
        bookDTO2.setImg("encodedImage 2");
        bookDTO2.setGenres(List.of(genreDTO1));

        reviewDTO = new ReviewDTO();
        reviewDTO.setDate(LocalDateTime.now());
        reviewDTO.setRating(4.5);
        reviewDTO.setReviewDescription("reviewDescription");

        savedReviewDTO = new ReviewDTO();
        savedReviewDTO.setId(1L);
        savedReviewDTO.setDate(LocalDateTime.now());
        savedReviewDTO.setRating(4.5);
        savedReviewDTO.setReviewDescription("reviewDescription");
        savedReviewDTO.setPersonEmail(personEmail);
        savedReviewDTO.setPersonFirstName("First Name");
    }

    @Test
    void findAll_shouldReturnAllBooksPaginated() throws Exception {

        List<BookDTO> pageContent = List.of(bookDTO1, bookDTO2);
        Pageable pageable = PageRequest.of(page, booksPerPage);
        Page<BookDTO> bookDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookService.findAll(any(Pageable.class))).thenReturn(bookDTOPage);

        mockMvc.perform(get(baseURL)
                        .param("page", String.valueOf(page))
                        .param("books-per-page", String.valueOf(booksPerPage))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDTOPage)));

        verify(bookService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void findById_shouldReturnBookDtoById() throws Exception {

        String url = baseURL + "/{bookId}";

        when(bookService.findById(any(Long.class))).thenReturn(bookDTO1);

        mockMvc.perform(get(url, bookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDTO1)));

        verify(bookService, times(1)).findById(any(Long.class));
    }

    @Test
    void findById_shouldReturnNotFoundIfBookIdIsIncorrect() throws Exception {

        String url = baseURL + "/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(bookService.findById(any(Long.class))).thenThrow(exception);

        mockMvc.perform(get(url, invalidBookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(bookService, times(1)).findById(any(Long.class));
    }

    @Test
    void findAllByTitle_shouldReturnAllBooksByTitlePaginated() throws Exception {

        String url = baseURL + "/search/by-title";

        List<BookDTO> pageContent = List.of(bookDTO1, bookDTO2);
        Pageable pageable = PageRequest.of(page, booksPerPage);
        Page<BookDTO> bookDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookService.findAllByTitle(any(String.class), any(Pageable.class))).thenReturn(bookDTOPage);

        mockMvc.perform(get(url)
                        .param("page", String.valueOf(page))
                        .param("books-per-page", String.valueOf(booksPerPage))
                        .param("title-query", "title")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDTOPage)));

        verify(bookService, times(1)).findAllByTitle(any(String.class), any(Pageable.class));
    }

    @Test
    void findAllByGenre_shouldReturnAllBooksByGenrePaginated() throws Exception {

        String url = baseURL + "/search/by-genre";

        List<BookDTO> pageContent = List.of(bookDTO1, bookDTO2);
        Pageable pageable = PageRequest.of(page, booksPerPage);
        Page<BookDTO> bookDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookService.findAllByGenre(any(String.class), any(Pageable.class))).thenReturn(bookDTOPage);

        mockMvc.perform(get(url)
                        .param("page", String.valueOf(page))
                        .param("books-per-page", String.valueOf(booksPerPage))
                        .param("genre-query", "genre")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookDTOPage)));

        verify(bookService, times(1)).findAllByGenre(any(String.class), any(Pageable.class));
    }

    @Test
    void findAllByGenre_shouldReturnNotFoundIfGenreIsIncorrect() throws Exception {

        String url = baseURL + "/search/by-genre";
        GenreException exception = new GenreException("No such genre found ", HttpStatus.NOT_FOUND);

        when(bookService.findAllByGenre(any(String.class), any(Pageable.class))).thenThrow(exception);

        mockMvc.perform(get(url)
                        .param("page", String.valueOf(page))
                        .param("books-per-page", String.valueOf(booksPerPage))
                        .param("genre-query", "incorrectGenre")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No such genre found "));

        verify(bookService, times(1)).findAllByGenre(any(String.class), any(Pageable.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnTrueIfBookIsCheckedOutByPerson() throws Exception {

        String url = baseURL + "/secure/is-checked-out/{bookId}";
        Boolean isBookCheckedOut = true;

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.isBookCheckedOutByPerson(any(String.class), any(Long.class))).thenReturn(isBookCheckedOut);

        mockMvc.perform(get(url, bookId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(isBookCheckedOut)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).isBookCheckedOutByPerson(any(String.class), any(Long.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/secure/is-checked-out/{bookId}";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(bookService.isBookCheckedOutByPerson(any(String.class), any(Long.class))).thenThrow(exception);

        mockMvc.perform(get(url, bookId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).isBookCheckedOutByPerson(any(String.class), any(Long.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/secure/is-checked-out/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.isBookCheckedOutByPerson(any(String.class), any(Long.class))).thenThrow(exception);

        mockMvc.perform(get(url, invalidBookId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).isBookCheckedOutByPerson(any(String.class), any(Long.class));
    }

    @Test
    void checkoutBook_shouldCreateCheckoutEntityAndUpdateBook() throws Exception {

        String url = baseURL + "/secure/checkout/{bookId}";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doNothing().when(bookService).checkoutBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).checkoutBook(any(String.class), any(Long.class));
    }

    @Test
    void checkoutBook_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/secure/checkout/{bookId}";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        doThrow(exception).when(bookService).checkoutBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).checkoutBook(any(String.class), any(Long.class));
    }

    @Test
    void checkoutBook_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/secure/checkout/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).checkoutBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, invalidBookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).checkoutBook(any(String.class), any(Long.class));
    }

    @Test
    void checkoutBook_shouldReturnForbiddenIfCopiesOrCopiesAvailableIsAlreadyZero() throws Exception {

        String url = baseURL + "/secure/checkout/{bookId}";
        BookException exception = new BookException("Book quantity is already 0 ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).checkoutBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Book quantity is already 0 "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).checkoutBook(any(String.class), any(Long.class));
    }

    @Test
    void checkoutBook_shouldReturnForbiddenIfCheckoutAlreadyExists() throws Exception {

        String url = baseURL + "/secure/checkout/{bookId}";
        BookException exception = new BookException("Book is already checked out by this user ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).checkoutBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Book is already checked out by this user "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).checkoutBook(any(String.class), any(Long.class));
    }

    @Test
    void checkoutBook_shouldReturnForbiddenIfPaymentIsGreaterThanZeroOrSomeBooksAreOverdue() throws Exception {

        String url = baseURL + "/secure/checkout/{bookId}";
        PaymentException exception = new PaymentException("You have outstanding fees / overdue books, checkout is unavailable", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).checkoutBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You have outstanding fees / overdue books, checkout is unavailable"));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).checkoutBook(any(String.class), any(Long.class));
    }

    @Test
    void renewCheckout_shouldUpdateCheckoutEntityAndUpdateBook() throws Exception {

        String url = baseURL + "/secure/renew-checkout/{bookId}";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doNothing().when(bookService).renewCheckout(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).renewCheckout(any(String.class), any(Long.class));
    }

    @Test
    void renewCheckout_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/secure/renew-checkout/{bookId}";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        doThrow(exception).when(bookService).renewCheckout(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).renewCheckout(any(String.class), any(Long.class));
    }

    @Test
    void renewCheckout_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/secure/renew-checkout/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).renewCheckout(any(String.class), any(Long.class));

        mockMvc.perform(put(url, invalidBookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).renewCheckout(any(String.class), any(Long.class));
    }

    @Test
    void renewCheckout_shouldReturnForbiddenIfBookIsNotCheckedOutByUser() throws Exception {

        String url = baseURL + "/secure/renew-checkout/{bookId}";
        BookException exception = new BookException("This book is not checked out by this user ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).renewCheckout(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This book is not checked out by this user "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).renewCheckout(any(String.class), any(Long.class));
    }

    @Test
    void renewCheckout_shouldReturnForbiddenIfBookIsOverdue() throws Exception {

        String url = baseURL + "/secure/renew-checkout/{bookId}";
        BookException exception = new BookException("This book is overdue ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).renewCheckout(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This book is overdue "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).renewCheckout(any(String.class), any(Long.class));
    }

    @Test
    void returnBook_shouldDeleteCheckoutEntityAndUpdateBookAndCreateHistoryRecordEntity() throws Exception {

        String url = baseURL + "/secure/return/{bookId}";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doNothing().when(bookService).returnBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).returnBook(any(String.class), any(Long.class));
    }

    @Test
    void returnBook_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/secure/return/{bookId}";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        doThrow(exception).when(bookService).returnBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).returnBook(any(String.class), any(Long.class));
    }

    @Test
    void returnBook_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/secure/return/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).returnBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, invalidBookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).returnBook(any(String.class), any(Long.class));
    }

    @Test
    void returnBook_shouldReturnForbiddenIfBookIsNotCheckedOutByUser() throws Exception {

        String url = baseURL + "/secure/return/{bookId}";
        BookException exception = new BookException("This book is not checked out by this user ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).returnBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This book is not checked out by this user "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).returnBook(any(String.class), any(Long.class));
    }

    @Test
    void returnBook_shouldReturnNotFoundIfPaymentInfoIsNotFound() throws Exception {

        String url = baseURL + "/secure/return/{bookId}";
        PaymentException exception = new PaymentException("Payment information is missing", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(bookService).returnBook(any(String.class), any(Long.class));

        mockMvc.perform(put(url, bookId)
                        .with(csrf())
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment information is missing"));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).returnBook(any(String.class), any(Long.class));
    }

    @Test
    void isBookReviewedByPerson_shouldReturnTrueIfBookIsReviewedByPerson() throws Exception {

        String url = baseURL + "/secure/is-reviewed/{bookId}";
        Boolean isBookReviewed = true;

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.isBookReviewedByPerson(any(String.class), any(Long.class))).thenReturn(isBookReviewed);

        mockMvc.perform(get(url, bookId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(isBookReviewed)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).isBookReviewedByPerson(any(String.class), any(Long.class));
    }

    @Test
    void isBookReviewedByPerson_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/secure/is-reviewed/{bookId}";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(bookService.isBookReviewedByPerson(any(String.class), any(Long.class))).thenThrow(exception);

        mockMvc.perform(get(url, bookId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).isBookReviewedByPerson(any(String.class), any(Long.class));
    }

    @Test
    void isBookReviewedByPerson_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/secure/is-reviewed/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.isBookReviewedByPerson(any(String.class), any(Long.class))).thenThrow(exception);

        mockMvc.perform(get(url, invalidBookId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).isBookReviewedByPerson(any(String.class), any(Long.class));
    }

    @Test
    void reviewBook_shouldCreateReviewEntityAndReturnSavedReviewDTO() throws Exception {

        String url = baseURL + "/secure/review/{bookId}";
        String dtoJson = objectMapper.writeValueAsString(reviewDTO);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class))).thenReturn(savedReviewDTO);

        mockMvc.perform(post(url, bookId)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(savedReviewDTO)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class));
    }

    @Test
    void reviewBook_shouldReturnForbiddenIfReviewDtoIsInvalid() throws Exception {

        String url = baseURL + "/secure/review/{bookId}";
        String dtoJson = objectMapper.writeValueAsString(reviewDTO);
        ReviewException exception = new ReviewException("Some fields are invalid. rating: Rating must be at least 0.5; ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url, bookId)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Some fields are invalid. rating: Rating must be at least 0.5; "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class));
    }

    @Test
    void reviewBook_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/secure/review/{bookId}";
        String dtoJson = objectMapper.writeValueAsString(reviewDTO);
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(bookService.reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url, bookId)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class));
    }

    @Test
    void reviewBook_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/secure/review/{bookId}";
        String dtoJson = objectMapper.writeValueAsString(reviewDTO);
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url, invalidBookId)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class));
    }

    @Test
    void reviewBook_shouldReturnForbiddenIfBookIsAlreadyReviewedByPerson() throws Exception {

        String url = baseURL + "/secure/review/{bookId}";
        String dtoJson = objectMapper.writeValueAsString(reviewDTO);
        ReviewException exception = new ReviewException("This book is already reviewed by this person ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(bookService.reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url, bookId)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This book is already reviewed by this person "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(bookService, times(1)).reviewBook(any(String.class), any(Long.class), any(ReviewDTO.class), any(BindingResult.class));
    }
}