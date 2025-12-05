package com.iliamalafeev.bookstore.bookstore_backend.utils.validators;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Component
public class BookValidator implements Validator {

    private final BookRepository bookRepository;

    @Autowired
    public BookValidator(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Book.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Book bookForValidation = (Book) target;
        Optional<Book> bookFromDB = bookRepository.findByTitleAndAuthor(bookForValidation.getTitle(), bookForValidation.getAuthor());

        if (bookFromDB.isPresent()) {

            if (bookFromDB.get().getAuthor().equals(bookForValidation.getAuthor())) {
                errors.rejectValue("title", "Book with this title from this author already exists");
            }
        }
    }
}
