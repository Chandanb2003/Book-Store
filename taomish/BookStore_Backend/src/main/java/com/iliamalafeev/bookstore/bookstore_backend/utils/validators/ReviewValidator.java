package com.iliamalafeev.bookstore.bookstore_backend.utils.validators;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Review;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ReviewValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Review.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Review reviewForValidation = (Review) target;

        if (reviewForValidation.getRating() == null || reviewForValidation.getRating() == 0) {
            errors.rejectValue("rating", "Rating must be at least 0.5");
        }
    }
}
