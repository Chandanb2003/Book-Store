package com.iliamalafeev.bookstore.bookstore_backend.utils.validators;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class PersonValidator implements Validator {

    private final PersonRepository personRepository;

    @Autowired
    public PersonValidator(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        Person personForValidation = (Person) target;

        if (personForValidation.getDateOfBirth().isBefore(LocalDate.of(1900, 1, 1))) {
            errors.rejectValue("dateOfBirth", "Birth date cannot be before 01-01-1900");
        }

        Optional<Person> personFromBD = personRepository.findByEmail(personForValidation.getEmail());

        if (personFromBD.isPresent()) {
            errors.rejectValue("email", "Person with this email is already registered");
        }
    }
}
