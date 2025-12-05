package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:5173/")
@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Controller")
public class BookController {

    private final BookService bookService;
    private final JwtUtils jwtUtils;

    @Autowired
    public BookController(BookService bookService, JwtUtils jwtUtils) {
        this.bookService = bookService;
        this.jwtUtils = jwtUtils;
    }

    private String extractEmail(String token) {
        String jwt = token.substring(7);
        return jwtUtils.extractPersonEmail(jwt);
    }

    @Operation(summary = "Get paginated list of books.",
            description = "Returns a Page containing BookDTO objects.")
    @GetMapping
    public ResponseEntity<Page<BookDTO>> findAll(@RequestParam(value = "page") Integer page,
                                                 @RequestParam(value = "books-per-page") Integer booksPerPage) {

        Page<BookDTO> responseBody = bookService.findAll(PageRequest.of(page, booksPerPage));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Get book by it's ID.",
            description = "Returns a JSON value of type BookDTO.")
    @GetMapping("/{bookId}")
    public ResponseEntity<BookDTO> findById(@PathVariable("bookId") Long bookId) {

        BookDTO responseBody = bookService.findById(bookId);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Get paginated list of books, found by title.",
            description = "Returns a Page containing BookDTO objects.")
    @GetMapping("/search/by-title")
    public ResponseEntity<Page<BookDTO>> findAllByTitle(@RequestParam(value = "page") Integer page,
                                                        @RequestParam(value = "books-per-page") Integer booksPerPage,
                                                        @RequestParam("title-query") String titleQuery) {

        Page<BookDTO> responseBody = bookService.findAllByTitle(titleQuery, PageRequest.of(page, booksPerPage));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Get paginated list of books, found by genre.",
            description = "Returns a Page containing BookDTO objects.")
    @GetMapping("/search/by-genre")
    public ResponseEntity<Page<BookDTO>> findAllByGenre(@RequestParam("genre-query") String genreQuery,
                                                        @RequestParam(value = "page") Integer page,
                                                        @RequestParam(value = "books-per-page") Integer booksPerPage) {

        Page<BookDTO> responseBody = bookService.findAllByGenre(genreQuery, PageRequest.of(page, booksPerPage));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Check if the book is checked out by authenticated user.",
            description = "Returns a Boolean value.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/secure/is-checked-out/{bookId}")
    public ResponseEntity<Boolean> isBookCheckedOutByPerson(@PathVariable("bookId") Long bookId,
                                                            @RequestHeader("Authorization") String token) {

        Boolean responseBody = bookService.isBookCheckedOutByPerson(extractEmail(token), bookId);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Check out the book.",
            description = "Creates new Checkout Entity and reduces book's copies available amount.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/secure/checkout/{bookId}")
    public ResponseEntity<HttpStatus> checkoutBook(@PathVariable("bookId") Long bookId,
                                                   @RequestHeader("Authorization") String token) {

        bookService.checkoutBook(extractEmail(token), bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Renew checkout for the book.",
            description = "Updates related Checkout Entity.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/secure/renew-checkout/{bookId}")
    public ResponseEntity<HttpStatus> renewCheckout(@PathVariable("bookId") Long bookId,
                                                    @RequestHeader("Authorization") String token) {

        bookService.renewCheckout(extractEmail(token), bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Return the book back to store.",
            description = "Updates user's payment amount if the book is outdated. Deletes related Checkout Entity. Creates new History Record Entity. Updates book's copies available amount")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/secure/return/{bookId}")
    public ResponseEntity<HttpStatus> returnBook(@PathVariable("bookId") Long bookId,
                                                 @RequestHeader("Authorization") String token) {

        bookService.returnBook(extractEmail(token), bookId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Check if the book is reviewed by authenticated user.",
            description = "Returns a Boolean value.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/secure/is-reviewed/{bookId}")
    public ResponseEntity<Boolean> isBookReviewedByPerson(@PathVariable("bookId") Long bookId,
                                                          @RequestHeader("Authorization") String token) {

        Boolean responseBody = bookService.isBookReviewedByPerson(extractEmail(token), bookId);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Create a Review for the book.",
            description = "Creates new Review Entity.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/secure/review/{bookId}")
    public ResponseEntity<ReviewDTO> reviewBook(@PathVariable("bookId") Long bookId,
                                                @RequestHeader("Authorization") String token,
                                                @RequestBody @Valid ReviewDTO reviewDTO,
                                                BindingResult bindingResult) {

        ReviewDTO responseBody = bookService.reviewBook(extractEmail(token), bookId, reviewDTO, bindingResult);
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }
}