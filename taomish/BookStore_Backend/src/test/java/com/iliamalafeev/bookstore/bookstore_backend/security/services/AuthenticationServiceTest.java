package com.iliamalafeev.bookstore.bookstore_backend.security.services;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonRegistrationDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.PersonDetails;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.validators.PersonValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private final String personEmail = "email@email.com";
    private final String password = "password";
    private final String encodedPassword = "encodedPassword";
    private final String testJWT = "encodedJwtContainingAllTheImportantInformation";

    private Person person;
    private PersonRegistrationDTO personRegistrationDTO;
    private PersonLoginDTO personLoginDTO;

    @Mock private PersonValidator personValidator;
    @Mock private PersonRepository personRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {

        personRegistrationDTO = new PersonRegistrationDTO("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, password);

        personLoginDTO = new PersonLoginDTO(personEmail, password);

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, password);
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    void registerPerson_shouldRegisterPersonAndReturnAuthenticationResponseWithJWT() {

        BindingResult bindingResult = new BindException(personRegistrationDTO, "personRegistrationDTO");

        when(passwordEncoder.encode(any(String.class))).thenReturn(encodedPassword);
        doNothing().when(personValidator).validate(any(Person.class), any(Errors.class));
        when(personRepository.save(any(Person.class))).thenReturn(person);
        when(jwtUtils.generateToken(any(PersonDetails.class))).thenReturn(testJWT);

        AuthenticationResponse response = assertDoesNotThrow(() -> authenticationService.registerPerson(personRegistrationDTO, bindingResult));

        assertNotNull(response);
        assertEquals(testJWT, response.getToken());
        verify(passwordEncoder, times(1)).encode(any(String.class));
        verify(personValidator, times(1)).validate(any(Person.class), any(Errors.class));
        verify(personRepository, times(1)).save(any(Person.class));
        verify(jwtUtils, times(1)).generateToken(any(PersonDetails.class));
    }

    @Test
    void registerPerson_shouldThrowPersonExceptionIfPersonRegistrationDtoIsInvalid() {

        BindingResult bindingResult = new BindException(personRegistrationDTO, "personRegistrationDTO");
        bindingResult.addError(new FieldError("personRegistrationDTO", "email", "Person with this email is already registered"));

        when(passwordEncoder.encode(any(String.class))).thenReturn(encodedPassword);
        doNothing().when(personValidator).validate(any(Person.class), any(Errors.class));

        PersonException exception = assertThrows(PersonException.class, () -> authenticationService.registerPerson(personRegistrationDTO, bindingResult));

        assertEquals("Some fields are invalid. email: Person with this email is already registered; ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(passwordEncoder, times(1)).encode(any(String.class));
        verify(personValidator, times(1)).validate(any(Person.class), any(Errors.class));
        verify(personRepository, times(0)).save(any(Person.class));
        verify(jwtUtils, times(0)).generateToken(any(PersonDetails.class));
    }

    @Test
    void authenticatePerson_shouldCheckCredentialsAndReturnAuthenticationResponseWithJWT() {

        BindingResult bindingResult = new BindException(personLoginDTO, "personLoginDTO");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new UsernamePasswordAuthenticationToken(personEmail, password));
        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(jwtUtils.generateToken(any(PersonDetails.class))).thenReturn(testJWT);

        AuthenticationResponse response = assertDoesNotThrow(() -> authenticationService.authenticatePerson(personLoginDTO, bindingResult));

        assertNotNull(response);
        assertEquals(testJWT, response.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(jwtUtils, times(1)).generateToken(any(PersonDetails.class));
    }

    @Test
    void authenticatePerson_shouldThrowPersonExceptionIfPersonLoginDtoIsInvalid() {

        BindingResult bindingResult = new BindException(personLoginDTO, "personLoginDTO");
        bindingResult.addError(new FieldError("personLoginDTO", "email", "This field must be formatted as Email address"));

        PersonException exception = assertThrows(PersonException.class, () -> authenticationService.authenticatePerson(personLoginDTO, bindingResult));

        assertEquals("Some fields are invalid. email: This field must be formatted as Email address; ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(authenticationManager, times(0)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(personRepository, times(0)).findByEmail(any(String.class));
        verify(jwtUtils, times(0)).generateToken(any(PersonDetails.class));
    }

    @Test
    void authenticatePerson_shouldThrowPersonExceptionIfCredentialsAreInvalid() {

        BindingResult bindingResult = new BindException(personLoginDTO, "personLoginDTO");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("Oops, bad credentials! :-( "));

        PersonException exception = assertThrows(PersonException.class, () -> authenticationService.authenticatePerson(personLoginDTO, bindingResult));

        assertEquals("Login or password is incorrect. ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(personRepository, times(0)).findByEmail(any(String.class));
        verify(jwtUtils, times(0)).generateToken(any(PersonDetails.class));
    }

    @Test
    void authenticatePerson_shouldThrowPersonExceptionIfPersonWithSuchEmailDoesNotExist() {

        BindingResult bindingResult = new BindException(personLoginDTO, "personLoginDTO");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(new UsernamePasswordAuthenticationToken(personEmail, password));
        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> authenticationService.authenticatePerson(personLoginDTO, bindingResult));

        assertEquals("Person with such email is not found. Please check the input fields. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(jwtUtils, times(0)).generateToken(any(PersonDetails.class));
    }
}