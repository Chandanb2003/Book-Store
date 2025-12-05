package com.iliamalafeev.bookstore.bookstore_backend.security.services;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonRegistrationDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.PersonDetails;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.utils.ErrorsUtil;
import com.iliamalafeev.bookstore.bookstore_backend.utils.validators.PersonValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class AuthenticationService {

    private final PersonValidator personValidator;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(PersonValidator personValidator, PersonRepository personRepository, PasswordEncoder passwordEncoder,
                                 JwtUtils jwtUtils, AuthenticationManager authenticationManager) {

        this.personValidator = personValidator;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse registerPerson(PersonRegistrationDTO personRegistrationDTO, BindingResult bindingResult) {

        Person person = new Person();

        person.setFirstName(personRegistrationDTO.getFirstName());
        person.setLastName(personRegistrationDTO.getLastName());
        person.setDateOfBirth(personRegistrationDTO.getDateOfBirth());
        person.setEmail(personRegistrationDTO.getEmail());
        person.setPassword(passwordEncoder.encode(personRegistrationDTO.getPassword()));
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.of("UTC")).toLocalDateTime());

        personValidator.validate(person, bindingResult);

        if (bindingResult.hasErrors()) {
            ErrorsUtil.returnPersonError("Some fields are invalid.", bindingResult, HttpStatus.FORBIDDEN);
        }

        personRepository.save(person);

        String jwtToken = jwtUtils.generateToken(new PersonDetails(person));

        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticatePerson(PersonLoginDTO personLoginDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ErrorsUtil.returnPersonError("Some fields are invalid.", bindingResult, HttpStatus.FORBIDDEN);
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(personLoginDTO.getEmail(), personLoginDTO.getPassword()));
        } catch (BadCredentialsException e) {
            ErrorsUtil.returnPersonError("Login or password is incorrect.", bindingResult, HttpStatus.FORBIDDEN);
        }

        Optional<Person> person = personRepository.findByEmail(personLoginDTO.getEmail());

        // This case is actually handled by "catch (BadCredentialsException e)" above
        if (person.isEmpty()) {
            ErrorsUtil.returnPersonError("Person with such email is not found. Please check the input fields.", bindingResult, HttpStatus.NOT_FOUND);
        }

        String jwtToken = jwtUtils.generateToken(new PersonDetails(person.get()));

        return new AuthenticationResponse(jwtToken);
    }
}
