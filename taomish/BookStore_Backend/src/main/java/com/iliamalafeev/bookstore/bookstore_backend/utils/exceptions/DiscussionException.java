package com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DiscussionException extends RuntimeException {

    private final HttpStatus httpStatus;

    public DiscussionException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
