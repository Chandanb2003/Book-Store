package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
//@Testcontainers
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class ReviewRepositoryTest {

//    @Container
//    @ServiceConnection
//    static PostgreSQLContainer<?> postgre = new PostgreSQLContainer<>("postgres:alpine");

    private Book book;
    private Review review1;
    private Review review2;

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    @Autowired
    ReviewRepositoryTest(ReviewRepository reviewRepository, BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }

    @BeforeEach
    void setUp() {

        book = new Book("Title", "Author", "Description", 10, 10, "encodedImage");
        book.setGenres(List.of(new Genre("Genre 1")));

        review1 = new Review("email1@email.com", "First Name 1", "Last Name 1", book, LocalDateTime.now(), 4.5, "Description 1");
        review2 = new Review("email2@email.com", "First Name 2", "Last Name 2", book, LocalDateTime.now(), 3.5, "Description 2");

        bookRepository.save(book);
    }

//    @Test
//    void connectionEstablished() {
//        assertTrue(postgre.isCreated());
//        assertTrue(postgre.isRunning());
//    }

    @Test
    public void save_shouldSaveReviewToDatabase() {

        Review savedReview = reviewRepository.save(review1);

        assertNotNull(savedReview);
        assertEquals(review1.getPersonEmail(), savedReview.getPersonEmail());
        assertEquals(review1.getPersonFirstName(), savedReview.getPersonFirstName());
        assertEquals(review1.getPersonLastName(), savedReview.getPersonLastName());
        assertEquals(review1.getDate(), savedReview.getDate());
        assertEquals(review1.getReviewDescription(), savedReview.getReviewDescription());
        assertTrue(savedReview.getId() > 0);
    }

    @Test
    void findAllByReviewedBookOrderByIdDesc_shouldReturnAllReviewsByReviewedBookOrderedByIdDescPaginated() {

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        Page<Review> reviews = reviewRepository.findAllByReviewedBookOrderByIdDesc(book, PageRequest.of(0, 5));

        assertNotNull(reviews);
        assertEquals(2, reviews.getTotalElements());
        assertTrue(reviews.getContent().get(0).getId() > reviews.getContent().get(1).getId());
        assertEquals(review2, reviews.getContent().get(0));
        assertEquals(review1, reviews.getContent().get(1));
    }

    @Test
    void findByReviewedBook_shouldReturnAllReviewsByReviewedBookPaginated() {

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        Page<Review> reviews = reviewRepository.findByReviewedBook(book, PageRequest.of(0, 5));

        assertNotNull(reviews);
        assertEquals(2, reviews.getTotalElements());
        assertEquals(review1, reviews.getContent().get(0));
        assertEquals(review2, reviews.getContent().get(1));
    }

    @Test
    void findByPersonEmailAndReviewedBook_shouldReturnReviewOptionalByPersonEmailAndReviewedBook() {

        reviewRepository.save(review1);

        Optional<Review> review = reviewRepository.findByPersonEmailAndReviewedBook("email1@email.com", book);

        assertNotNull(review);
        assertTrue(review.isPresent());
        assertEquals(review1.getPersonEmail(), review.get().getPersonEmail());
        assertEquals(review1.getPersonFirstName(), review.get().getPersonFirstName());
        assertEquals(review1.getPersonLastName(), review.get().getPersonLastName());
        assertEquals(review1.getDate(), review.get().getDate());
        assertEquals(review1.getRating(), review.get().getRating());
        assertEquals(review1.getReviewDescription(), review.get().getReviewDescription());
        assertTrue(review.get().getId() > 0);
        assertEquals(review1.getReviewedBook(), review.get().getReviewedBook());
    }

    @Test
    void findByPersonEmailAndReviewedBook_shouldReturnEmptyReviewOptionalIfPersonEmailIsIncorrect() {

        reviewRepository.save(review1);

        Optional<Review> reviewOptional1 = reviewRepository.findByPersonEmailAndReviewedBook("incorrectEmail@email.com", book);

        assertNotNull(reviewOptional1);
        assertTrue(reviewOptional1.isEmpty());
    }

    @Test
    void getAverageRatingByReviewedBook() {

        reviewRepository.save(review1);
        reviewRepository.save(review2);

        Double rating = reviewRepository.getAverageRatingByReviewedBook(book);

        assertNotNull(rating);
        assertEquals(rating, (review1.getRating() + review2.getRating()) / 2);
    }
}