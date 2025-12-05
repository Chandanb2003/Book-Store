package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.CheckoutDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Checkout;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.CheckoutRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";

    private Person person;
    private Checkout checkout1;
    private Checkout checkout2;
    private List<Checkout> checkouts;
    private BookDTO bookDTO;

    @Mock private ModelMapper modelMapper;
    @Mock private CheckoutRepository checkoutRepository;
    @Mock private PersonRepository personRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());

        Book book1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        book1.setGenres(List.of(new Genre("Genre 1")));

        Book book2 = new Book("Title 2", "Author 2", "Description 2", 10, 10, "encodedImage 2");
        book2.setGenres(List.of(new Genre("Genre 2")));

        checkout1 = new Checkout(person, book1, LocalDate.now(), LocalDate.now().plusDays(7));
        checkout2 = new Checkout(person, book2, LocalDate.now(), LocalDate.now().plusDays(7));

        checkouts = List.of(checkout1, checkout2);

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setDescription("Genre 1");

        bookDTO = new BookDTO();
        bookDTO.setTitle("Title 1");
        bookDTO.setAuthor("Author 1");
        bookDTO.setDescription("Description 1");
        bookDTO.setCopies(10);
        bookDTO.setCopiesAvailable(10);
        bookDTO.setImg("encodedImage 1");
        bookDTO.setGenres(List.of(genreDTO));
    }

    @Test
    void getCurrentCheckoutsCount_shouldReturnCountOfCheckoutsMadeByPerson() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(checkoutRepository.findByCheckoutHolder(any(Person.class))).thenReturn(checkouts);

        int returnedCount = checkoutService.getCurrentCheckoutsCount(personEmail);

        assertEquals(2, returnedCount);
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolder(any(Person.class));
    }

    @Test
    void getCurrentCheckoutsCount_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> checkoutService.getCurrentCheckoutsCount(invalidPersonEmail));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
    }

    @Test
    void getCurrentCheckouts_shouldReturnAllCurrentCheckoutsMadeByPerson() {

        int checkout1DaysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), checkout1.getReturnDate());
        int checkout2DaysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), checkout2.getReturnDate());

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(checkoutRepository.findByCheckoutHolder(any(Person.class))).thenReturn(checkouts);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO);

        List<CheckoutDTO> checkoutDTOs = checkoutService.getCurrentCheckouts(personEmail);

        assertNotNull(checkoutDTOs);
        assertEquals(checkouts.size(), checkoutDTOs.size());
        assertEquals(checkout1DaysLeft, checkoutDTOs.get(0).getDaysLeft());
        assertEquals(checkout2DaysLeft, checkoutDTOs.get(1).getDaysLeft());
        assertEquals(bookDTO, checkoutDTOs.get(0).getBookDTO());
        assertEquals(bookDTO, checkoutDTOs.get(1).getBookDTO());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolder(any(Person.class));
        verify(modelMapper, times(2)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void getCurrentCheckouts_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> checkoutService.getCurrentCheckouts(invalidPersonEmail));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
        verify(modelMapper, times(0)).map(any(Book.class), eq(BookDTO.class));
    }
}