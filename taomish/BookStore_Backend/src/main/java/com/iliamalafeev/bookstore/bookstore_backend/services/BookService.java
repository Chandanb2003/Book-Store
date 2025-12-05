package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.*;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.*;
import com.iliamalafeev.bookstore.bookstore_backend.utils.ErrorsUtil;
import com.iliamalafeev.bookstore.bookstore_backend.utils.validators.BookValidator;
import com.iliamalafeev.bookstore.bookstore_backend.utils.validators.ReviewValidator;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final ModelMapper modelMapper;
    private final BookValidator bookValidator;
    private final ReviewValidator reviewValidator;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final CheckoutRepository checkoutRepository;
    private final PersonRepository personRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final HistoryRecordRepository historyRecordRepository;

    @Autowired
    public BookService(ModelMapper modelMapper, BookValidator bookValidator, ReviewValidator reviewValidator, BookRepository bookRepository,
                       GenreRepository genreRepository, CheckoutRepository checkoutRepository, PersonRepository personRepository,
                       PaymentRepository paymentRepository, ReviewRepository reviewRepository, HistoryRecordRepository historyRecordRepository) {

        this.modelMapper = modelMapper;
        this.bookValidator = bookValidator;
        this.reviewValidator = reviewValidator;
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.checkoutRepository = checkoutRepository;
        this.personRepository = personRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.historyRecordRepository = historyRecordRepository;
    }

//  <------------------------------------------------------------------------------->
//  <-------------------- Service public methods for controller -------------------->
//  <------------------------------------------------------------------------------->

    public Page<BookDTO> findAll(Pageable pageable) {

        Page<Book> page = bookRepository.findAll(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));

        return page.map(this::convertToBookDTO);
    }

    public BookDTO findById(Long bookId) {

        Book book = getBookFromRepository(bookId);

        return convertToBookDTO(book);
    }

    public Page<BookDTO> findAllByTitle(String titleQuery, Pageable pageable) {

        return bookRepository.findByTitleContainingIgnoreCase(titleQuery, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .map(this::convertToBookDTO);
    }

    public Page<BookDTO> findAllByGenre(String genreQuery, Pageable pageable) {

        Optional<Genre> genre = genreRepository.findByDescription(genreQuery);

        if (genre.isEmpty()) {
            ErrorsUtil.returnGenreError("No such genre found", null, HttpStatus.NOT_FOUND);
        }

        return bookRepository.findByGenresContains(genre.get(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()))
                .map(this::convertToBookDTO);
    }

    @Transactional
    public BookDTO addBook(BookDTO bookDTO, BindingResult bindingResult) {

        Book book = convertToBook(bookDTO);

        bookValidator.validate(book, bindingResult);

        if (bindingResult.hasErrors()) {
            ErrorsUtil.returnBookError("Some fields are invalid.", bindingResult, HttpStatus.FORBIDDEN);
        }

        List<String> genreDescriptions = bookDTO.getGenres().stream().map(GenreDTO::getDescription).toList();
        List<Genre> genres = genreRepository.findByDescriptionIn(genreDescriptions);

        if (genres.size() != genreDescriptions.size()) {
            ErrorsUtil.returnGenreError("No such genres found", null, HttpStatus.NOT_FOUND);
        }

        book.setGenres(genres);
        genres.forEach(genre -> genre.getBooks().add(book));
        Book savedBook = bookRepository.save(book);

        return convertToBookDTO(savedBook);
    }

    @Transactional
    public void deleteById(Long bookId) {

        bookRepository.deleteById(bookId);
    }

    @Transactional
    public void changeQuantity(Long bookId, String operation) {

        Book book = getBookFromRepository(bookId);

        if (operation.equals("increase")) {

            book.setCopiesAvailable(book.getCopiesAvailable() + 1);
            book.setCopies(book.getCopies() + 1);
            bookRepository.save(book);
        }

        if (operation.equals("decrease")) {

            if (book.getCopiesAvailable() <= 0 || book.getCopies() <= 0) {
                ErrorsUtil.returnBookError("Book quantity is already 0", null, HttpStatus.FORBIDDEN);
            }

            book.setCopiesAvailable(book.getCopiesAvailable() - 1);
            book.setCopies(book.getCopies() - 1);
            bookRepository.save(book);
        }
    }

    public boolean isBookCheckedOutByPerson(String personEmail, Long bookId) {

        Person person = getPersonFromRepository(personEmail);
        Book book = getBookFromRepository(bookId);
        Optional<Checkout> checkout = getCheckoutOptionalFromRepository(person, book);

        return checkout.isPresent();
    }

    @Transactional
    public void checkoutBook(String personEmail, Long bookId) {

        Person person = getPersonFromRepository(personEmail);
        Book book = getBookFromRepository(bookId);
        Optional<Checkout> checkout = getCheckoutOptionalFromRepository(person, book);

        if (book.getCopiesAvailable() <= 0 || book.getCopies() <= 0) {
            ErrorsUtil.returnBookError("Book quantity is already 0", null, HttpStatus.FORBIDDEN);
        }

        if (checkout.isPresent()) {
            ErrorsUtil.returnBookError("Book is already checked out by this user", null, HttpStatus.FORBIDDEN);
        }

        List<Checkout> currentCheckouts = checkoutRepository.findByCheckoutHolder(person);
        boolean bookNeedsReturned = false;

        for (Checkout currentCheckout : currentCheckouts) {

            LocalDate d1 = currentCheckout.getReturnDate();
            LocalDate d2 = LocalDate.now();

            if (d2.isAfter(d1)) {
                bookNeedsReturned = true;
                break;
            }
        }

        Optional<Payment> payment = getPaymentOptionalFromRepository(person);

        if (payment.isPresent() && (payment.get().getAmount() > 0 || bookNeedsReturned)) {
            ErrorsUtil.returnPaymentError("You have outstanding fees / overdue books, checkout is unavailable", HttpStatus.FORBIDDEN);
        }

        if (payment.isEmpty()) {
            Payment newPayment = new Payment(person, 00.00);
            paymentRepository.save(newPayment);
        }

        Checkout newCheckout = new Checkout(person, book, LocalDate.now(), LocalDate.now().plusDays(7));
        checkoutRepository.save(newCheckout);

        book.setCopiesAvailable(book.getCopiesAvailable() - 1);
        book.getCheckouts().add(newCheckout);
        bookRepository.save(book);
    }

    @Transactional
    public void renewCheckout(String personEmail, Long bookId) {

        Person person = getPersonFromRepository(personEmail);
        Book book = getBookFromRepository(bookId);
        Optional<Checkout> checkout = getCheckoutOptionalFromRepository(person, book);

        if (checkout.isEmpty()) {
            ErrorsUtil.returnBookError("This book is not checked out by this user", null, HttpStatus.FORBIDDEN);
        }

        LocalDate d1 = checkout.get().getReturnDate();
        LocalDate d2 = LocalDate.now();

        if (d1.isAfter(d2) || d1.isEqual(d2)) {
            checkout.get().setReturnDate(LocalDate.now().plusDays(7));
            checkoutRepository.save(checkout.get());
        }

        if (d1.isBefore(d2)) {
            ErrorsUtil.returnBookError("This book is overdue", null, HttpStatus.FORBIDDEN);
        }
    }

    @Transactional
    public void returnBook(String personEmail, Long bookId) {

        Person person = getPersonFromRepository(personEmail);
        Book book = getBookFromRepository(bookId);
        Optional<Checkout> checkout = getCheckoutOptionalFromRepository(person, book);

        if (checkout.isEmpty()) {
            ErrorsUtil.returnBookError("This book is not checked out by this user", null, HttpStatus.FORBIDDEN);
        }

        LocalDate d1 = checkout.get().getReturnDate();
        LocalDate d2 = LocalDate.now();

        if (d1.isBefore(d2)) {

            Optional<Payment> paymentOptional = getPaymentOptionalFromRepository(person);

            if (paymentOptional.isEmpty()) {
                ErrorsUtil.returnPaymentError("Payment information is missing", HttpStatus.NOT_FOUND);
            }

            Payment payment = paymentOptional.get();

            payment.setAmount(payment.getAmount() + (int) ChronoUnit.DAYS.between(d1, d2));
            paymentRepository.save(payment);
        }

        HistoryRecord historyRecord = new HistoryRecord(person, book, checkout.get().getCheckoutDate(), LocalDate.now());
        historyRecordRepository.save(historyRecord);

        book.getCheckouts().remove(checkout.get());
        book.getHistoryRecords().add(historyRecord);
        book.setCopiesAvailable(book.getCopiesAvailable() + 1);
        bookRepository.save(book);

        checkoutRepository.deleteById(checkout.get().getId());
    }

    public boolean isBookReviewedByPerson(String personEmail, Long bookId) {

        Book book = getBookFromRepository(bookId);
        Optional<Review> review = reviewRepository.findByPersonEmailAndReviewedBook(personEmail, book);

        return review.isPresent();
    }

    @Transactional
    public ReviewDTO reviewBook(String personEmail, Long bookId, ReviewDTO reviewDTO, BindingResult bindingResult) {

        Review newReview = convertToReview(reviewDTO);

        reviewValidator.validate(newReview, bindingResult);

        if (bindingResult.hasErrors()) {
            ErrorsUtil.returnReviewError("Some fields are invalid.", bindingResult, HttpStatus.FORBIDDEN);
        }

        Person person = getPersonFromRepository(personEmail);

        Book book = getBookFromRepository(bookId);
        Optional<Review> review = reviewRepository.findByPersonEmailAndReviewedBook(personEmail, book);

        if (review.isPresent()) {
            ErrorsUtil.returnReviewError("This book is already reviewed by this person", null, HttpStatus.FORBIDDEN);
        }

        newReview.setPersonEmail(personEmail);
        newReview.setPersonFirstName(person.getFirstName());
        newReview.setPersonLastName(person.getLastName());
        newReview.setReviewedBook(book);

        Review savedReview = reviewRepository.save(newReview);

        return convertToReviewDTO(savedReview);
    }

//  <-------------------------------------------------------------------------------------------->
//  <-------------------- Service private methods for some code re-usability -------------------->
//  <-------------------------------------------------------------------------------------------->

    private Book getBookFromRepository(Long bookId) {

        Optional<Book> book = bookRepository.findById(bookId);

        if (book.isEmpty()) {
            ErrorsUtil.returnBookError("Book not found", null, HttpStatus.NOT_FOUND);
        }

        return book.get();
    }

    private Person getPersonFromRepository(String personEmail) {

        Optional<Person> person = personRepository.findByEmail(personEmail);

        if (person.isEmpty()) {
            ErrorsUtil.returnPersonError("Person with such email is not found.", null, HttpStatus.NOT_FOUND);
        }

        return person.get();
    }

    private Optional<Checkout> getCheckoutOptionalFromRepository(Person person, Book book) {

        return checkoutRepository.findByCheckoutHolderAndCheckedOutBook(person, book);
    }

    private Optional<Payment> getPaymentOptionalFromRepository(Person person) {

        return paymentRepository.findByPaymentHolder(person);
    }

    private Book convertToBook(BookDTO bookDTO) {
        return modelMapper.map(bookDTO, Book.class);
    }

    private BookDTO convertToBookDTO(Book book) {
        return modelMapper.map(book, BookDTO.class);
    }

    private Review convertToReview(ReviewDTO reviewDTO) {
        return modelMapper.map(reviewDTO, Review.class);
    }

    private ReviewDTO convertToReviewDTO(Review review) {
        return modelMapper.map(review, ReviewDTO.class);
    }
}