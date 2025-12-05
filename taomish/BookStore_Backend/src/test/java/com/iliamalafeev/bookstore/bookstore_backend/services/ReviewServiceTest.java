package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Review;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.BookRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.ReviewRepository;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.BookException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private final Long bookId = 1L;
    private final Long invalidBookId = 1000L;
    private final int page = 0;
    private final int reviewsPerPage = 5;

    private Book book1;
    private Review review1;
    private Review review2;
    private ReviewDTO reviewDTO;

    @Mock private ModelMapper modelMapper;
    @Mock private ReviewRepository reviewRepository;
    @Mock private BookRepository bookRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {

        String personEmail = "email@email.com";

        book1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        book1.setGenres(List.of(new Genre("Genre 1")));

        review1 = new Review(personEmail, "First Name 1", "Last Name 1", book1, LocalDateTime.now(), 4.5, "Description 1");
        review2 = new Review(personEmail, "First Name 2", "Last Name 2", book1, LocalDateTime.now(), 3.5, "Description 2");

        reviewDTO = new ReviewDTO();
        reviewDTO.setPersonEmail(personEmail);
        reviewDTO.setPersonFirstName("firstName");
        review1.setDate(LocalDateTime.now());
        review1.setRating(4.5);
        review1.setReviewDescription("description");
    }

    @Test
    void findAllByBookId_shouldReturnAllReviewsByBookIdPaginatedAndOrderedById() {

        List<Review> pageContent = List.of(review1, review2);
        Pageable pageable = PageRequest.of(page, reviewsPerPage);
        Page<Review> reviewsPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.findAllByReviewedBookOrderByIdDesc(any(Book.class), any(Pageable.class))).thenReturn(reviewsPage);
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);

        Page<ReviewDTO> reviewDTOPage = reviewService.findAllByBookId(bookId, pageable, true);

        assertNotNull(reviewDTOPage);
        assertEquals(pageContent.size(), reviewDTOPage.getContent().size());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).findAllByReviewedBookOrderByIdDesc(any(Book.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void findAllByBookId_shouldReturnAllReviewsByBookIdPaginatedAndNotOrderedById() {

        List<Review> pageContent = List.of(review1, review2);
        Pageable pageable = PageRequest.of(page, reviewsPerPage);
        Page<Review> reviewsPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.findByReviewedBook(any(Book.class), any(Pageable.class))).thenReturn(reviewsPage);
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);

        Page<ReviewDTO> reviewDTOPage = reviewService.findAllByBookId(bookId, pageable, false);

        assertNotNull(reviewDTOPage);
        assertEquals(pageContent.size(), reviewDTOPage.getContent().size());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).findByReviewedBook(any(Book.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void findAllByBookId_shouldThrowBookExceptionIfBookIdIsInvalid() {

        Pageable pageable = PageRequest.of(page, reviewsPerPage);

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> reviewService.findAllByBookId(invalidBookId, pageable, true));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(0)).findByReviewedBook(any(Book.class), any(Pageable.class));
        verify(modelMapper, times(0)).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void getAverageRatingByBookId_shouldReturnAverageRatingOfAllBookReviewsByBookId() {

        double avgRating = (review1.getRating() + review2.getRating()) / 2;

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.getAverageRatingByReviewedBook(any(Book.class))).thenReturn(avgRating);

        double returnedAvgRating = reviewService.getAverageRatingByBookId(bookId);

        assertEquals(avgRating, returnedAvgRating);
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).getAverageRatingByReviewedBook(any(Book.class));
    }

    @Test
    void getAverageRatingByBookId_shouldReturnZeroIfThereAreNoReviews() {

        double avgRating = 00.00;

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.getAverageRatingByReviewedBook(any(Book.class))).thenReturn(null);

        double returnedAvgRating = reviewService.getAverageRatingByBookId(bookId);

        assertEquals(avgRating, returnedAvgRating);
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).getAverageRatingByReviewedBook(any(Book.class));
    }

    @Test
    void getAverageRatingByBookId_shouldThrowBookExceptionIfBookIdIsInvalid() {

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> reviewService.getAverageRatingByBookId(invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(0)).getAverageRatingByReviewedBook(any(Book.class));
    }
}