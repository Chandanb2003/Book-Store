package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Checkout;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class CheckoutRepositoryTest {

    private Person checkoutHolder;
    private Book checkedOutBook1;
    private Book checkedOutBook2;
    private Checkout checkout1;
    private Checkout checkout2;

    private final PersonRepository personRepository;
    private final BookRepository bookRepository;
    private final CheckoutRepository checkoutRepository;

    @Autowired
    CheckoutRepositoryTest(PersonRepository personRepository, BookRepository bookRepository, CheckoutRepository checkoutRepository) {
        this.personRepository = personRepository;
        this.bookRepository = bookRepository;
        this.checkoutRepository = checkoutRepository;
    }

    @BeforeEach
    void setUp() {

        checkoutHolder = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), "email@email.com", "Password");
        checkoutHolder.setRole(Role.ROLE_USER);
        checkoutHolder.setRegisteredAt(LocalDateTime.now());

        checkedOutBook1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        checkedOutBook1.setGenres(List.of(new Genre("Genre 1")));

        checkedOutBook2 = new Book("Title 2", "Author 2", "Description 2", 10, 10, "encodedImage 2");
        checkedOutBook2.setGenres(List.of(new Genre("Genre 2")));

        checkout1 = new Checkout(checkoutHolder, checkedOutBook1, LocalDate.now(), LocalDate.now().plusDays(7));
        checkout2 = new Checkout(checkoutHolder, checkedOutBook2, LocalDate.now(), LocalDate.now().plusDays(7));

        personRepository.save(checkoutHolder);
        bookRepository.save(checkedOutBook1);
        bookRepository.save(checkedOutBook2);
    }

    @Test
    void save_shouldSaveCheckoutToDatabase() {

        Checkout savedCheckout = checkoutRepository.save(checkout1);

        assertNotNull(savedCheckout);
        assertEquals(checkout1.getCheckoutHolder(), savedCheckout.getCheckoutHolder());
        assertEquals(checkout1.getCheckedOutBook(), savedCheckout.getCheckedOutBook());
        assertEquals(checkout1.getCheckoutDate(), savedCheckout.getCheckoutDate());
        assertEquals(checkout1.getReturnDate(), savedCheckout.getReturnDate());
        assertTrue(checkout1.getId() > 0);
    }

    @Test
    void deleteById_shouldDeleteCheckoutWithGivenIdFromDatabase() {

        Checkout savedCheckout = checkoutRepository.save(checkout1);
        checkoutRepository.deleteById(savedCheckout.getId());

        assertTrue(checkoutRepository.findById(savedCheckout.getId()).isEmpty());
    }

    @Test
    void findByCheckoutHolderAndCheckedOutBook_shouldReturnCheckoutOptionalByCheckoutHolderAndCheckedOutBook() {

        Checkout savedCheckout = checkoutRepository.save(checkout1);

        Optional<Checkout> checkout = checkoutRepository.findByCheckoutHolderAndCheckedOutBook(checkoutHolder, checkedOutBook1);

        assertNotNull(checkout);
        assertTrue(checkout.isPresent());
        assertEquals(checkout.get(), savedCheckout);
    }

    @Test
    void findByCheckoutHolderAndCheckedOutBook_shouldReturnEmptyCheckoutOptionalIfCheckedOutBookIsIncorrect() {

        checkoutRepository.save(checkout1);

        Optional<Checkout> checkout = checkoutRepository.findByCheckoutHolderAndCheckedOutBook(checkoutHolder, checkedOutBook2);

        assertNotNull(checkout);
        assertTrue(checkout.isEmpty());
    }

    @Test
    void findByCheckoutHolder_shouldReturnListOfCheckoutsByCheckoutHolder() {

        checkoutRepository.save(checkout1);
        checkoutRepository.save(checkout2);

        List<Checkout> checkouts = checkoutRepository.findByCheckoutHolder(checkoutHolder);
        assertNotNull(checkouts);
        assertEquals(checkouts.size(), 2);
        assertTrue(checkouts.contains(checkout1));
        assertTrue(checkouts.contains(checkout2));
        assertEquals(checkout1, checkouts.get(0));
        assertEquals(checkout2, checkouts.get(1));
    }
}