package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person("First Name", "Last Name", 
                LocalDate.of(1990, 1, 1), 
                "email@email.com", 
                "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());
    }

    @Test
    public void save_shouldSavePersonToDatabase() {
        Person savedPerson = personRepository.save(person);

        assertNotNull(savedPerson);
        assertEquals(person.getFirstName(), savedPerson.getFirstName());
        assertEquals(person.getLastName(), savedPerson.getLastName());
        assertEquals(person.getDateOfBirth(), savedPerson.getDateOfBirth());
        assertEquals(person.getEmail(), savedPerson.getEmail());
        assertEquals(person.getPassword(), savedPerson.getPassword());
        assertTrue(savedPerson.getId() > 0);
    }

    @Test
    public void findByEmail_shouldFindPersonByEmail() {
        personRepository.save(person);
        Person foundPerson = personRepository.findByEmail("email@email.com")
                .orElse(null);

        assertNotNull(foundPerson);
        assertEquals(person.getFirstName(), foundPerson.getFirstName());
        assertEquals(person.getLastName(), foundPerson.getLastName());
        assertEquals(person.getDateOfBirth(), foundPerson.getDateOfBirth());
        assertEquals(person.getEmail(), foundPerson.getEmail());
        assertEquals(person.getPassword(), foundPerson.getPassword());
        assertTrue(foundPerson.getId() > 0);
    }

    @Test
    public void findByEmail_shouldNotFindPersonByIncorrectEmail() {
        Person foundPerson = personRepository.findByEmail("incorrectEmail@email.com")
                .orElse(null);

        assertNull(foundPerson);
    }
}

