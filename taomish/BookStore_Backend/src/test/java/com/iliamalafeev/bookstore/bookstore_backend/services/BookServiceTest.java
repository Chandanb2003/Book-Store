package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.*;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.*;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.*;
import com.iliamalafeev.bookstore.bookstore_backend.utils.validators.BookValidator;
import com.iliamalafeev.bookstore.bookstore_backend.utils.validators.ReviewValidator;
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
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private final Long bookId = 1L;
    private final Long invalidBookId = 1000L;
    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";
    private final int page = 0;
    private final int booksPerPage = 5;

    private Genre genre1;
    private Book book1;
    private Book book2;
    private BookDTO bookDTO1;
    private Person person;
    private Checkout checkout;
    private Payment payment;
    private ReviewDTO reviewDTO;
    private Review review;

    @Mock private ModelMapper modelMapper;
    @Mock private BookValidator bookValidator;
    @Mock private ReviewValidator reviewValidator;
    @Mock private BookRepository bookRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private CheckoutRepository checkoutRepository;
    @Mock private PersonRepository personRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private HistoryRecordRepository historyRecordRepository;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {

        genre1 = new Genre("Genre 1");
        Genre genre2 = new Genre("Genre 2");

        GenreDTO genreDTO1 = new GenreDTO();
        genreDTO1.setDescription("Genre 1");

        book1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        book1.setGenres(List.of(genre1));

        book2 = new Book("Title 2", "Author 2", "Description 2", 10, 10, "encodedImage 2");
        book2.setGenres(List.of(genre2));

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());

        checkout = new Checkout(person, book1, LocalDate.now().minusDays(5), LocalDate.now().plusDays(2));

        payment = new Payment(person, 00.00);

        review = new Review(null, null, null, null, LocalDateTime.now(), 4.5, "Review Description");

        bookDTO1 = new BookDTO();
        bookDTO1.setTitle("Title 1");
        bookDTO1.setAuthor("Author 1");
        bookDTO1.setDescription("Description 1");
        bookDTO1.setCopies(10);
        bookDTO1.setCopiesAvailable(10);
        bookDTO1.setImg("encodedImage 1");
        bookDTO1.setGenres(List.of(genreDTO1));
        
        reviewDTO = new ReviewDTO();
        reviewDTO.setDate(LocalDateTime.now());
        reviewDTO.setRating(4.5);
        reviewDTO.setReviewDescription("reviewDescription");
    }

    @Test
    void findAll_shouldReturnAllBooksPaginated() {

        List<Book> pageContent = List.of(book1, book2);
        Pageable pageable = PageRequest.of(page, booksPerPage);
        Page<Book> booksPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(booksPage);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO1);

        Page<BookDTO> bookDTOPage = bookService.findAll(pageable);

        assertNotNull(bookDTOPage);
        assertEquals(pageContent.size(), bookDTOPage.getContent().size());
        verify(bookRepository, times(1)).findAll(any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void findById_shouldReturnBookDtoById() {

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO1);

        BookDTO returnedBookDTO = bookService.findById(bookId);

        assertNotNull(returnedBookDTO);
        assertEquals(bookDTO1.getId(), returnedBookDTO.getId());
        assertEquals(bookDTO1.getTitle(), returnedBookDTO.getTitle());
        assertEquals(bookDTO1.getAuthor(), returnedBookDTO.getAuthor());
        assertEquals(bookDTO1.getDescription(), returnedBookDTO.getDescription());
        assertEquals(bookDTO1.getCopies(), returnedBookDTO.getCopies());
        assertEquals(bookDTO1.getCopiesAvailable(), returnedBookDTO.getCopiesAvailable());
        assertEquals(bookDTO1.getImg(), returnedBookDTO.getImg());
        assertEquals(bookDTO1.getGenres(), returnedBookDTO.getGenres());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(modelMapper, times(1)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void findById_shouldThrowBookExceptionIfBookIdIsIncorrect() {

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.findById(invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(modelMapper, times(0)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void findAllByTitle_shouldReturnAllBooksByTitlePaginated() {

        List<Book> pageContent = List.of(book1, book2);
        Pageable pageable = PageRequest.of(page, booksPerPage);
        Page<Book> booksPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(bookRepository.findByTitleContainingIgnoreCase(any(String.class), any(Pageable.class))).thenReturn(booksPage);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO1);

        Page<BookDTO> bookDTOPage = bookService.findAllByTitle("title", pageable);

        assertNotNull(bookDTOPage);
        assertEquals(pageContent.size(), bookDTOPage.getContent().size());
        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(any(String.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void findAllByGenre_shouldReturnAllBooksByGenrePaginated() {

        book1.setGenres(List.of(genre1));
        book2.setGenres(List.of(genre1));

        List<Book> pageContent = List.of(book1, book2);
        Pageable pageable = PageRequest.of(page, booksPerPage);
        Page<Book> booksPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(genreRepository.findByDescription(any(String.class))).thenReturn(Optional.of(genre1));
        when(bookRepository.findByGenresContains(any(Genre.class), any(Pageable.class))).thenReturn(booksPage);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO1);

        Page<BookDTO> bookDTOPage = bookService.findAllByGenre(genre1.getDescription(), pageable);

        assertNotNull(bookDTOPage);
        assertEquals(pageContent.size(), bookDTOPage.getContent().size());
        verify(genreRepository, times(1)).findByDescription(any(String.class));
        verify(bookRepository, times(1)).findByGenresContains(any(Genre.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void findAllByGenre_shouldThrowGenreExceptionIfGenreIsIncorrect() {

        Pageable pageable = PageRequest.of(page, booksPerPage);

        when(genreRepository.findByDescription(any(String.class))).thenReturn(Optional.empty());

        GenreException exception = assertThrows(GenreException.class, () -> bookService.findAllByGenre("Incorrect genre", pageable));
        assertEquals("No such genre found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(genreRepository, times(1)).findByDescription(any(String.class));
        verify(bookRepository, times(0)).findByGenresContains(any(Genre.class), any(Pageable.class));
        verify(modelMapper, times(0)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void addBook_shouldAddBookToDatabaseAndReturnSavedBookDTO() {

        genre1.setBooks(new ArrayList<>(Collections.singleton(book1)));

        BindingResult bindingResult = new BindException(bookDTO1, "bookDTO");

        when(modelMapper.map(any(BookDTO.class), eq(Book.class))).thenReturn(book1);
        doNothing().when(bookValidator).validate(any(Book.class), any(Errors.class));
        when(genreRepository.findByDescriptionIn(anyList())).thenReturn(List.of(genre1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO1);

        BookDTO savedBookDTO = assertDoesNotThrow(() -> bookService.addBook(bookDTO1, bindingResult));

        assertNotNull(savedBookDTO);
        assertEquals(bookDTO1.getTitle(), savedBookDTO.getTitle());
        assertEquals(bookDTO1.getAuthor(), savedBookDTO.getAuthor());
        assertEquals(bookDTO1.getDescription(), savedBookDTO.getDescription());
        assertEquals(bookDTO1.getCopies(), savedBookDTO.getCopies());
        assertEquals(bookDTO1.getCopiesAvailable(), savedBookDTO.getCopiesAvailable());
        assertEquals(bookDTO1.getImg(), savedBookDTO.getImg());
        assertEquals(bookDTO1.getGenres(), savedBookDTO.getGenres());
        verify(modelMapper, times(1)).map(any(BookDTO.class), eq(Book.class));
        verify(bookValidator, times(1)).validate(any(Book.class), any(Errors.class));
        verify(genreRepository, times(1)).findByDescriptionIn(anyList());
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(modelMapper, times(1)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void addBook_shouldThrowBookExceptionIfBookDtoIsInvalid() {

        BindingResult bindingResult = new BindException(bookDTO1, "bookDTO");
        bindingResult.addError(new FieldError("bookDTO", "title", "Book with this title from this author already exists"));

        when(modelMapper.map(any(BookDTO.class), eq(Book.class))).thenReturn(book1);
        doNothing().when(bookValidator).validate(any(Book.class), any(Errors.class));

        BookException exception = assertThrows(BookException.class, () -> bookService.addBook(bookDTO1, bindingResult));

        assertEquals("Some fields are invalid. title: Book with this title from this author already exists; ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(modelMapper, times(1)).map(any(BookDTO.class), eq(Book.class));
        verify(bookValidator, times(1)).validate(any(Book.class), any(Errors.class));
        verify(genreRepository, times(0)).findByDescriptionIn(anyList());
        verify(bookRepository, times(0)).save(any(Book.class));
        verify(modelMapper, times(0)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void addBook_shouldThrowGenreExceptionIfGenresAreInvalid() {

        BindingResult bindingResult = new BindException(bookDTO1, "bookDTO");

        when(modelMapper.map(any(BookDTO.class), eq(Book.class))).thenReturn(book1);
        doNothing().when(bookValidator).validate(any(Book.class), any(Errors.class));
        when(genreRepository.findByDescriptionIn(anyList())).thenReturn(List.of());

        GenreException exception = assertThrows(GenreException.class, () -> bookService.addBook(bookDTO1, bindingResult));
        assertEquals("No such genres found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(modelMapper, times(1)).map(any(BookDTO.class), eq(Book.class));
        verify(bookValidator, times(1)).validate(any(Book.class), any(Errors.class));
        verify(genreRepository, times(1)).findByDescriptionIn(anyList());
        verify(bookRepository, times(0)).save(any(Book.class));
        verify(modelMapper, times(0)).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void deleteById_shouldDeleteBookByIdFromDatabase() {

        doNothing().when(bookRepository).deleteById(any(Long.class));

        assertDoesNotThrow(() -> bookService.deleteById(bookId));
        verify(bookRepository, times(1)).deleteById(any(Long.class));
    }

    @Test
    void changeQuantity_shouldIncreaseBookQuantityIfOperationIsIncrease() {

        String operation = "increase";

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        assertDoesNotThrow(() -> bookService.changeQuantity(bookId, operation));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void changeQuantity_shouldDecreaseBookQuantityIfOperationIsDecrease() {

        String operation = "decrease";

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        assertDoesNotThrow(() -> bookService.changeQuantity(bookId, operation));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void changeQuantity_shouldNotSaveIfOperationIsInvalid() {

        String operation = "Invalid operation";

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));

        assertDoesNotThrow(() -> bookService.changeQuantity(bookId, operation));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void changeQuantity_shouldThrowBookExceptionIfBookIdIsInvalid() {

        String operation = "increase";

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.changeQuantity(invalidBookId, operation));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void changeQuantity_shouldThrowBookExceptionIfCopiesIsAlreadyZero() {

        String operation = "decrease";
        book1.setCopies(0);

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));

        BookException exception = assertThrows(BookException.class, () -> bookService.changeQuantity(bookId, operation));
        assertEquals("Book quantity is already 0 ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void changeQuantity_shouldThrowBookExceptionIfCopiesAvailableIsAlreadyZero() {

        String operation = "decrease";
        book1.setCopiesAvailable(0);

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));

        BookException exception = assertThrows(BookException.class, () -> bookService.changeQuantity(bookId, operation));
        assertEquals("Book quantity is already 0 ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnTrueIfBookIsCheckedOutByPerson() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.of(checkout));

        boolean isCheckedOut = bookService.isBookCheckedOutByPerson(personEmail, bookId);

        assertTrue(isCheckedOut);
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldReturnFalseIfBookIsNotCheckedOutByPerson() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());

        boolean isCheckedOut = bookService.isBookCheckedOutByPerson(personEmail, bookId);

        assertFalse(isCheckedOut);
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> bookService.isBookCheckedOutByPerson(invalidPersonEmail, invalidBookId));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(0)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
    }

    @Test
    void isBookCheckedOutByPerson_shouldThrowBookExceptionIfBookIdIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.isBookCheckedOutByPerson(personEmail, invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
    }

    @Test
    void checkoutBook_shouldCreateCheckoutEntityAndUpdateBook() {

        List<Checkout> checkouts = new ArrayList<>(Collections.singletonList(checkout));
        book1.setCheckouts(checkouts);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());
        when(checkoutRepository.findByCheckoutHolder(any(Person.class))).thenReturn(checkouts);
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(checkout);
        when(bookRepository.save(any(Book.class))).thenReturn(book1);

        assertDoesNotThrow(() -> bookService.checkoutBook(personEmail, bookId));
        assertEquals(book1.getCopies() - 1, book1.getCopiesAvailable());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(checkoutRepository, times(1)).save(any(Checkout.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> bookService.checkoutBook(invalidPersonEmail, invalidBookId));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(0)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowBookExceptionIfBookIdIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.checkoutBook(personEmail, invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowBookExceptionIfCopiesIsAlreadyZero() {

        book1.setCopies(0);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.checkoutBook(personEmail, bookId));
        assertEquals("Book quantity is already 0 ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowBookExceptionIfCopiesAvailableIsAlreadyZero() {

        book1.setCopiesAvailable(0);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.checkoutBook(personEmail, bookId));
        assertEquals("Book quantity is already 0 ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowBookExceptionIfCheckoutAlreadyExists() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.of(checkout));

        BookException exception = assertThrows(BookException.class, () -> bookService.checkoutBook(personEmail, bookId));
        assertEquals("Book is already checked out by this user ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowPaymentExceptionIfPaymentExistsAndSomeBooksAreOverdue() {

        checkout.setReturnDate(LocalDate.now().minusDays(1));
        List<Checkout> checkouts = new ArrayList<>(Collections.singletonList(checkout));
        book1.setCheckouts(checkouts);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());
        when(checkoutRepository.findByCheckoutHolder(any(Person.class))).thenReturn(checkouts);
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.of(payment));

        PaymentException exception = assertThrows(PaymentException.class, () -> bookService.checkoutBook(personEmail, bookId));
        assertEquals("You have outstanding fees / overdue books, checkout is unavailable", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void checkoutBook_shouldThrowPaymentExceptionIfPaymentExistsAndIsGreaterThanZero() {

        List<Checkout> checkouts = new ArrayList<>(Collections.singletonList(checkout));
        book1.setCheckouts(checkouts);

        payment.setAmount(10.00);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());
        when(checkoutRepository.findByCheckoutHolder(any(Person.class))).thenReturn(checkouts);
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.of(payment));

        PaymentException exception = assertThrows(PaymentException.class, () -> bookService.checkoutBook(personEmail, bookId));
        assertEquals("You have outstanding fees / overdue books, checkout is unavailable", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolder(any(Person.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
        verify(bookRepository, times(0)).save(any(Book.class));
    }

    @Test
    void renewCheckout_shouldUpdateCheckoutEntityAndUpdateBook() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.of(checkout));
        when(checkoutRepository.save(any(Checkout.class))).thenReturn(checkout);

        assertDoesNotThrow(() -> bookService.renewCheckout(personEmail, bookId));
        assertEquals(LocalDate.now().plusDays(7), checkout.getReturnDate());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(1)).save(any(Checkout.class));
    }

    @Test
    void renewCheckout_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> bookService.renewCheckout(invalidPersonEmail, invalidBookId));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(0)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
    }

    @Test
    void renewCheckout_shouldThrowBookExceptionIfBookIdIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.renewCheckout(personEmail, invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
    }

    @Test
    void renewCheckout_shouldThrowBookExceptionIfBookIsNotCheckedOutByUser() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.renewCheckout(personEmail, bookId));
        assertEquals("This book is not checked out by this user ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
    }

    @Test
    void renewCheckout_shouldThrowBookExceptionIfBookIsOverdue() {

        checkout.setReturnDate(LocalDate.now().minusDays(3));

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.of(checkout));

        BookException exception = assertThrows(BookException.class, () -> bookService.renewCheckout(personEmail, bookId));
        assertEquals("This book is overdue ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(checkoutRepository, times(0)).save(any(Checkout.class));
    }

    @Test
    void returnBook_shouldDeleteCheckoutEntityAndUpdateBookAndCreateHistoryRecordEntity() {

        checkout.setId(1L);
        checkout.setReturnDate(LocalDate.now().minusDays(3));
        List<Checkout> checkouts = new ArrayList<>(Collections.singletonList(checkout));
        book1.setCheckouts(checkouts);

        HistoryRecord historyRecord = new HistoryRecord(person, book1, checkout.getCheckoutDate(), LocalDate.now());
        List<HistoryRecord> historyRecords = new ArrayList<>();
        book1.setHistoryRecords(historyRecords);

        double expectedPaymentAmount = payment.getAmount() + (int) ChronoUnit.DAYS.between(checkout.getReturnDate(), LocalDate.now());

        assertEquals(1, book1.getCheckouts().size());
        assertEquals(0, book1.getHistoryRecords().size());
        assertEquals(book1.getCopies(), book1.getCopiesAvailable());
        assertEquals(00.00, payment.getAmount());

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.of(checkout));
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(historyRecordRepository.save(any(HistoryRecord.class))).thenReturn(historyRecord);
        when(bookRepository.save(any(Book.class))).thenReturn(book1);
        doNothing().when(checkoutRepository).deleteById(any(Long.class));

        assertDoesNotThrow(() -> bookService.returnBook(personEmail, bookId));
        assertEquals(0, book1.getCheckouts().size());
        assertEquals(1, book1.getHistoryRecords().size());
        assertEquals(book1.getCopies() + 1, book1.getCopiesAvailable());
        assertEquals(expectedPaymentAmount, payment.getAmount());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(historyRecordRepository, times(1)).save(any(HistoryRecord.class));
        verify(bookRepository, times(1)).save(any(Book.class));
        verify(checkoutRepository, times(1)).deleteById(any(Long.class));
    }

    @Test
    void returnBook_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> bookService.returnBook(invalidPersonEmail, invalidBookId));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(0)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(historyRecordRepository, times(0)).save(any(HistoryRecord.class));
        verify(bookRepository, times(0)).save(any(Book.class));
        verify(checkoutRepository, times(0)).deleteById(any(Long.class));
    }

    @Test
    void returnBook_shouldThrowBookExceptionIfBookIdIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.returnBook(personEmail, invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(0)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(historyRecordRepository, times(0)).save(any(HistoryRecord.class));
        verify(bookRepository, times(0)).save(any(Book.class));
        verify(checkoutRepository, times(0)).deleteById(any(Long.class));
    }

    @Test
    void returnBook_shouldThrowBookExceptionIfBookIsNotCheckedOutByUser() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.returnBook(personEmail, bookId));
        assertEquals("This book is not checked out by this user ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(historyRecordRepository, times(0)).save(any(HistoryRecord.class));
        verify(bookRepository, times(0)).save(any(Book.class));
        verify(checkoutRepository, times(0)).deleteById(any(Long.class));
    }

    @Test
    void returnBook_shouldThrowPaymentExceptionIfPaymentInfoIsNotFound() {

        checkout.setReturnDate(LocalDate.now().minusDays(3));

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(checkoutRepository.findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class))).thenReturn(Optional.of(checkout));
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> bookService.returnBook(personEmail, bookId));

        assertEquals("Payment information is missing", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(checkoutRepository, times(1)).findByCheckoutHolderAndCheckedOutBook(any(Person.class), any(Book.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
        verify(historyRecordRepository, times(0)).save(any(HistoryRecord.class));
        verify(bookRepository, times(0)).save(any(Book.class));
        verify(checkoutRepository, times(0)).deleteById(any(Long.class));
    }

    @Test
    void isBookReviewedByPerson_shouldReturnTrueIfBookIsReviewedByPerson() {

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.findByPersonEmailAndReviewedBook(any(String.class), any(Book.class))).thenReturn(Optional.of(review));

        boolean isReviewed = bookService.isBookReviewedByPerson(personEmail, bookId);

        assertTrue(isReviewed);
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
    }

    @Test
    void isBookReviewedByPerson_shouldReturnFalseIfBookIsNotReviewedByPerson() {

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.findByPersonEmailAndReviewedBook(any(String.class), any(Book.class))).thenReturn(Optional.empty());

        boolean isReviewed = bookService.isBookReviewedByPerson(personEmail, bookId);

        assertFalse(isReviewed);
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
    }

    @Test
    void isBookReviewedByPerson_shouldThrowBookExceptionIfBookIdIsInvalid() {

        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.isBookReviewedByPerson(personEmail, invalidBookId));
        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(0)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
    }

    @Test
    void reviewBook_shouldCreateReviewEntityAndReturnSavedReviewDTO() {

        BindingResult bindingResult = new BindException(reviewDTO, "reviewDTO");

        assertNull(review.getPersonEmail());
        assertNull(review.getPersonFirstName());
        assertNull(review.getPersonLastName());
        assertNull(review.getReviewedBook());

        when(modelMapper.map(any(ReviewDTO.class), eq(Review.class))).thenReturn(review);
        doNothing().when(reviewValidator).validate(any(Review.class), any(Errors.class));
        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.findByPersonEmailAndReviewedBook(any(String.class), any(Book.class))).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(modelMapper.map(any(Review.class), eq(ReviewDTO.class))).thenReturn(reviewDTO);

        ReviewDTO savedReviewDTO = assertDoesNotThrow(() -> bookService.reviewBook(personEmail, bookId, reviewDTO, bindingResult));

        assertNotNull(savedReviewDTO);
        assertEquals(reviewDTO.getPersonFirstName(), savedReviewDTO.getPersonFirstName());
        assertEquals(reviewDTO.getPersonEmail(), savedReviewDTO.getPersonEmail());
        assertEquals(reviewDTO.getRating(), savedReviewDTO.getRating());
        assertEquals(reviewDTO.getReviewDescription(), savedReviewDTO.getReviewDescription());
        assertEquals(reviewDTO.getDate(), savedReviewDTO.getDate());
        assertEquals(person.getEmail(), review.getPersonEmail());
        assertEquals(person.getFirstName(), review.getPersonFirstName());
        assertEquals(person.getLastName(), review.getPersonLastName());
        assertEquals(book1, review.getReviewedBook());
        verify(modelMapper, times(1)).map(any(ReviewDTO.class), eq(Review.class));
        verify(reviewValidator, times(1)).validate(any(Review.class), any(Errors.class));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(modelMapper, times(1)).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void reviewBook_shouldThrowReviewExceptionIfReviewDtoIsInvalid() {

        BindingResult bindingResult = new BindException(reviewDTO, "reviewDTO");
        bindingResult.addError(new FieldError("bookDTO", "rating", "Rating must be at least 0.5"));

        when(modelMapper.map(any(ReviewDTO.class), eq(Review.class))).thenReturn(review);
        doNothing().when(reviewValidator).validate(any(Review.class), any(Errors.class));

        ReviewException exception = assertThrows(ReviewException.class, () -> bookService.reviewBook(personEmail, bookId, reviewDTO, bindingResult));

        assertEquals("Some fields are invalid. rating: Rating must be at least 0.5; ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(modelMapper, times(1)).map(any(ReviewDTO.class), eq(Review.class));
        verify(reviewValidator, times(1)).validate(any(Review.class), any(Errors.class));
        verify(personRepository, times(0)).findByEmail(any(String.class));
        verify(bookRepository, times(0)).findById(any(Long.class));
        verify(reviewRepository, times(0)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
        verify(reviewRepository, times(0)).save(any(Review.class));
        verify(modelMapper, times(0)).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void reviewBook_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        BindingResult bindingResult = new BindException(reviewDTO, "reviewDTO");

        when(modelMapper.map(any(ReviewDTO.class), eq(Review.class))).thenReturn(review);
        doNothing().when(reviewValidator).validate(any(Review.class), any(Errors.class));
        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> bookService.reviewBook(invalidPersonEmail, bookId, reviewDTO, bindingResult));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(modelMapper, times(1)).map(any(ReviewDTO.class), eq(Review.class));
        verify(reviewValidator, times(1)).validate(any(Review.class), any(Errors.class));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(0)).findById(any(Long.class));
        verify(reviewRepository, times(0)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
        verify(reviewRepository, times(0)).save(any(Review.class));
        verify(modelMapper, times(0)).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void reviewBook_shouldThrowBookExceptionIfBookIdIsInvalid() {

        BindingResult bindingResult = new BindException(reviewDTO, "reviewDTO");

        when(modelMapper.map(any(ReviewDTO.class), eq(Review.class))).thenReturn(review);
        doNothing().when(reviewValidator).validate(any(Review.class), any(Errors.class));
        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        BookException exception = assertThrows(BookException.class, () -> bookService.reviewBook(personEmail, bookId, reviewDTO, bindingResult));

        assertEquals("Book not found ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(modelMapper, times(1)).map(any(ReviewDTO.class), eq(Review.class));
        verify(reviewValidator, times(1)).validate(any(Review.class), any(Errors.class));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(0)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
        verify(reviewRepository, times(0)).save(any(Review.class));
        verify(modelMapper, times(0)).map(any(Review.class), eq(ReviewDTO.class));
    }

    @Test
    void reviewBook_shouldThrowReviewExceptionIfBookIsAlreadyReviewedByPerson() {

        BindingResult bindingResult = new BindException(reviewDTO, "reviewDTO");

        when(modelMapper.map(any(ReviewDTO.class), eq(Review.class))).thenReturn(review);
        doNothing().when(reviewValidator).validate(any(Review.class), any(Errors.class));
        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(bookRepository.findById(any(Long.class))).thenReturn(Optional.of(book1));
        when(reviewRepository.findByPersonEmailAndReviewedBook(any(String.class), any(Book.class))).thenReturn(Optional.of(review));

        ReviewException exception = assertThrows(ReviewException.class, () -> bookService.reviewBook(personEmail, bookId, reviewDTO, bindingResult));

        assertEquals("This book is already reviewed by this person ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(modelMapper, times(1)).map(any(ReviewDTO.class), eq(Review.class));
        verify(reviewValidator, times(1)).validate(any(Review.class), any(Errors.class));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(bookRepository, times(1)).findById(any(Long.class));
        verify(reviewRepository, times(1)).findByPersonEmailAndReviewedBook(any(String.class), any(Book.class));
        verify(reviewRepository, times(0)).save(any(Review.class));
        verify(modelMapper, times(0)).map(any(Review.class), eq(ReviewDTO.class));
    }
}