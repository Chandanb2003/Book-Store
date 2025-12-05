package com.iliamalafeev.bookstore.bookstore_backend.security.services;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonDetailsServiceTest {

    private final String personEmail = "email@email.com";

    private Person person;

    @Mock private PersonRepository personRepository;

    @InjectMocks
    private PersonDetailsService personDetailsService;

    @BeforeEach
    void setUp() {

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_shouldReturnPersonDetailsObjectWithPersonDetails() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));

        UserDetails userDetails = assertDoesNotThrow(() -> personDetailsService.loadUserByUsername(personEmail));

        assertNotNull(userDetails);
        assertEquals(person.getEmail(), userDetails.getUsername());
        assertEquals(person.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(personRepository, times(1)).findByEmail(any(String.class));
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundExceptionIfNoPersonFoundWithSuchEmail() {

        when(personRepository.findByEmail(personEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> personDetailsService.loadUserByUsername(personEmail));

        assertNotNull(exception);
        assertEquals("No users found with such email.", exception.getMessage());
        verify(personRepository, times(1)).findByEmail(any(String.class));
    }
}