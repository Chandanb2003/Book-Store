package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class BookRepositoryTest {

    private Book book1;
    private Book book2;

    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    @Autowired
    BookRepositoryTest(BookRepository bookRepository, GenreRepository genreRepository) {
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
    }

    @BeforeEach
    void setUp() {

        Genre genre1 = new Genre("Genre 1");
        Genre genre2 = new Genre("Genre 2");

        book1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        book1.setGenres(List.of(genre1));

        book2 = new Book("Title 2", "Author 2", "Description 2", 10, 10, "encodedImage 2");
        book2.setGenres(List.of(genre2));

        genre1.setBooks(List.of(book1));
        genre2.setBooks(List.of(book2));

        genreRepository.save(genre1);
        genreRepository.save(genre2);
    }

    @Test
    void save_shouldSaveBookToDatabase() {

        Book savedBook = bookRepository.save(book1);

        assertNotNull(savedBook);
        assertEquals(book1.getTitle(), savedBook.getTitle());
        assertEquals(book1.getAuthor(), savedBook.getAuthor());
        assertEquals(book1.getDescription(), savedBook.getDescription());
        assertEquals(book1.getCopies(), savedBook.getCopies());
        assertEquals(book1.getCopiesAvailable(), savedBook.getCopiesAvailable());
        assertEquals(book1.getImg(), savedBook.getImg());
        assertEquals(book1.getGenres(), savedBook.getGenres());
        assertTrue(savedBook.getId() > 0);
    }

    @Test
    void findById_shouldReturnBookOptionalById() {

        Book savedBook = bookRepository.save(book1);

        Optional<Book> book = bookRepository.findById(savedBook.getId());

        assertNotNull(book);
        assertTrue(book.isPresent());
        assertEquals(savedBook, book.get());
    }

    @Test
    void findById_shouldReturnEmptyBookOptionalIfIdIsIncorrect() {

        Book savedBook = bookRepository.save(book1);

        Optional<Book> book = bookRepository.findById(savedBook.getId() + 100);

        assertNotNull(book);
        assertTrue(book.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllBooksFromDatabasePaginated() {

        bookRepository.save(book1);
        bookRepository.save(book2);

        Page<Book> books = bookRepository.findAll(PageRequest.of(0, 5));

        assertNotNull(books);
        assertEquals(2, books.getTotalElements());
        assertEquals(book1, books.getContent().get(0));
        assertEquals(book2, books.getContent().get(1));
    }

    @Test
    void deleteById_shouldDeleteBookFromDatabaseById() {

        Book savedBook = bookRepository.save(book1);
        bookRepository.deleteById(savedBook.getId());

        assertTrue(bookRepository.findById(savedBook.getId()).isEmpty());
    }

    @Test
    void findByTitleAndAuthor_shouldReturnBookOptionalByTitleAndAuthor() {

        Book savedBook = bookRepository.save(book1);

        Optional<Book> book = bookRepository.findByTitleAndAuthor(book1.getTitle(), book1.getAuthor());

        assertNotNull(book);
        assertTrue(book.isPresent());
        assertEquals(savedBook, book.get());
    }

    @Test
    void findByTitleAndAuthor_shouldReturnEmptyBookOptionalIfTitleIsIncorrect() {

        bookRepository.save(book1);

        Optional<Book> book = bookRepository.findByTitleAndAuthor("Incorrect Title", book1.getAuthor());

        assertNotNull(book);
        assertTrue(book.isEmpty());
    }

    @Test
    void findByTitleAndAuthor_shouldReturnEmptyBookOptionalIfAuthorIsIncorrect() {

        bookRepository.save(book1);

        Optional<Book> book = bookRepository.findByTitleAndAuthor(book1.getTitle(), "Incorrect Author");

        assertNotNull(book);
        assertTrue(book.isEmpty());
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnBooksByTitleContainingIgnoreCasePaginated() {

        bookRepository.save(book1);
        bookRepository.save(book2);

        Page<Book> books = bookRepository.findByTitleContainingIgnoreCase("ITL", PageRequest.of(0, 5));

        assertNotNull(books);
        assertEquals(2, books.getTotalElements());
        assertEquals(book1, books.getContent().get(0));
        assertEquals(book2, books.getContent().get(1));
    }

    @Test
    void findByGenresContains_shouldReturnBooksByGenresContainingPaginated() {

        bookRepository.save(book1);
        bookRepository.save(book2);

        Page<Book> genre1books = bookRepository.findByGenresContains(book1.getGenres().get(0), PageRequest.of(0, 5));
        Page<Book> genre2books = bookRepository.findByGenresContains(book2.getGenres().get(0), PageRequest.of(0, 5));

        assertNotNull(genre1books);
        assertNotNull(genre2books);
        assertEquals(1, genre1books.getTotalElements());
        assertEquals(1, genre2books.getTotalElements());
        assertEquals(book1, genre1books.getContent().get(0));
        assertEquals(book2, genre2books.getContent().get(0));
    }
}