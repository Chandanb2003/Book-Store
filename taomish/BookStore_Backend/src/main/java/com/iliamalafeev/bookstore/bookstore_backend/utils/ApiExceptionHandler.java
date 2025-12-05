package com.iliamalafeev.bookstore.bookstore_backend.utils;

import com.iliamalafeev.bookstore.bookstore_backend.utils.error_responses.*;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.*;
import com.stripe.exception.StripeException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExpiredJwtErrorResponse> handleExpiredJwtException() {
        ExpiredJwtErrorResponse response = new ExpiredJwtErrorResponse("Your authentication token is expired, please re-login.", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<SignatureErrorResponse> handleSignatureException() {
        SignatureErrorResponse response = new SignatureErrorResponse("Your authentication token is invalid or it's signature cannot be trusted, please re-login.", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<MalformedJwtErrorResponse> handleMalformedJwtException() {
        MalformedJwtErrorResponse response = new MalformedJwtErrorResponse("Your authentication token is invalid or malformed, please re-login.", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BookException.class)
    private ResponseEntity<BookErrorResponse> handleBookException(BookException e) {
        BookErrorResponse response = new BookErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }

    @ExceptionHandler(DiscussionException.class)
    private ResponseEntity<DiscussionErrorResponse> handleDiscussionException(DiscussionException e) {
        DiscussionErrorResponse response = new DiscussionErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }

    @ExceptionHandler(GenreException.class)
    private ResponseEntity<GenreErrorResponse> handleGenreException(GenreException e) {
        GenreErrorResponse response = new GenreErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }

    @ExceptionHandler(PaymentException.class)
    private ResponseEntity<PaymentErrorResponse> handlePaymentException(PaymentException e) {
        PaymentErrorResponse response = new PaymentErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }

    @ExceptionHandler(PersonException.class)
    private ResponseEntity<PersonErrorResponse> handlePersonException(PersonException e) {
        PersonErrorResponse response = new PersonErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }

    @ExceptionHandler(ReviewException.class)
    private ResponseEntity<ReviewErrorResponse> handleReviewException(ReviewException e) {
        ReviewErrorResponse response = new ReviewErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, e.getHttpStatus());
    }

    @ExceptionHandler(StripeException.class)
    private ResponseEntity<PaymentErrorResponse> handleStripeException(StripeException e) {
        PaymentErrorResponse response = new PaymentErrorResponse(e.getMessage(), System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
