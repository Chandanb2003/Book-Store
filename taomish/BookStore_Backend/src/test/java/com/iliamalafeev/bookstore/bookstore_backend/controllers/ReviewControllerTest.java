package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.ReviewService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.BookException;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@WithMockUser
class ReviewControllerTest {

    private final Long bookId = 1L;
    private final Long invalidBookId = 1000L;
    private final int page = 0;
    private final int reviewsPerPage = 5;
    private final String baseURL = "/api/reviews";

    private ReviewDTO reviewDTO1;
    private ReviewDTO reviewDTO2;

    @MockBean private ReviewService reviewService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    ReviewControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        reviewDTO1 = new ReviewDTO();
        reviewDTO1.setId(1L);
        reviewDTO1.setPersonEmail("email1@email.com");
        reviewDTO1.setPersonFirstName("First Name 1");
        reviewDTO1.setDate(LocalDateTime.now());
        reviewDTO1.setRating(4.5);
        reviewDTO1.setReviewDescription("Description 1");

        reviewDTO2 = new ReviewDTO();
        reviewDTO1.setId(2L);
        reviewDTO2.setPersonEmail("email1@email.com");
        reviewDTO2.setPersonFirstName("First Name 2");
        reviewDTO2.setDate(LocalDateTime.now());
        reviewDTO2.setRating(3.5);
        reviewDTO2.setReviewDescription("Description 2");
    }

    @Test
    void findAllByBookId_shouldReturnAllReviewsByBookIdPaginated() throws Exception {

        String url = baseURL + "/{bookId}";

        List<ReviewDTO> pageContent = List.of(reviewDTO1, reviewDTO2);
        Pageable pageable = PageRequest.of(page, reviewsPerPage);
        Page<ReviewDTO> reviewDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(reviewService.findAllByBookId(any(Long.class), any(Pageable.class), any(Boolean.class))).thenReturn(reviewDTOPage);

        mockMvc.perform(get(url, bookId)
                        .param("page", String.valueOf(page))
                        .param("reviews-per-page", String.valueOf(reviewsPerPage))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(reviewDTOPage)));

        verify(reviewService, times(1)).findAllByBookId(any(Long.class), any(Pageable.class), any(Boolean.class));
    }

    @Test
    void findAllByBookId_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(reviewService.findAllByBookId(any(Long.class), any(Pageable.class), any(Boolean.class))).thenThrow(exception);

        mockMvc.perform(get(url, invalidBookId)
                        .param("page", String.valueOf(page))
                        .param("reviews-per-page", String.valueOf(reviewsPerPage))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(reviewService, times(1)).findAllByBookId(any(Long.class), any(Pageable.class), any(Boolean.class));
    }

    @Test
    void getAverageRatingByBookId_shouldReturnAverageRatingOfAllBookReviewsByBookId() throws Exception {

        String url = baseURL + "/average-rating/{bookId}";
        Double avgRating = (reviewDTO1.getRating() + reviewDTO2.getRating()) / 2;

        when(reviewService.getAverageRatingByBookId(any(Long.class))).thenReturn(avgRating);

        mockMvc.perform(get(url, bookId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(avgRating)));

        verify(reviewService, times(1)).getAverageRatingByBookId(any(Long.class));
    }

    @Test
    void getAverageRatingByBookId_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/average-rating/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        when(reviewService.getAverageRatingByBookId(any(Long.class))).thenThrow(exception);

        mockMvc.perform(get(url, invalidBookId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(reviewService, times(1)).getAverageRatingByBookId(any(Long.class));
    }
}