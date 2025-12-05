package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.entities.HistoryRecord;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class HistoryRecordRepositoryTest {

    private Person historyRecordHolder;
    private HistoryRecord historyRecord1;
    private HistoryRecord historyRecord2;

    private final HistoryRecordRepository historyRecordRepository;
    private final PersonRepository personRepository;
    private final BookRepository bookRepository;

    @Autowired
    HistoryRecordRepositoryTest(HistoryRecordRepository historyRecordRepository, PersonRepository personRepository, BookRepository bookRepository) {
        this.historyRecordRepository = historyRecordRepository;
        this.personRepository = personRepository;
        this.bookRepository = bookRepository;
    }

    @BeforeEach
    void setUp() {

        historyRecordHolder = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), "email@email.com", "Password");
        historyRecordHolder.setRole(Role.ROLE_USER);
        historyRecordHolder.setRegisteredAt(LocalDateTime.now());

        Book historyRecordedBook1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        historyRecordedBook1.setGenres(List.of(new Genre("Genre 1")));

        Book historyRecordedBook2 = new Book("Title 2", "Author 2", "Description 2", 10, 10, "encodedImage 2");
        historyRecordedBook2.setGenres(List.of(new Genre("Genre 2")));

        historyRecord1 = new HistoryRecord(historyRecordHolder, historyRecordedBook1, LocalDate.now().minusDays(2), LocalDate.now());
        historyRecord2 = new HistoryRecord(historyRecordHolder, historyRecordedBook2, LocalDate.now().minusDays(2), LocalDate.now());

        personRepository.save(historyRecordHolder);
        bookRepository.save(historyRecordedBook1);
        bookRepository.save(historyRecordedBook2);
    }

    @Test
    void save_shouldSaveHistoryRecordToDatabase() {

        HistoryRecord savedHistoryRecord = historyRecordRepository.save(historyRecord1);

        assertNotNull(savedHistoryRecord);
        assertEquals(historyRecord1.getHistoryRecordHolder(), savedHistoryRecord.getHistoryRecordHolder());
        assertEquals(historyRecord1.getHistoryRecordedBook(), savedHistoryRecord.getHistoryRecordedBook());
        assertEquals(historyRecord1.getCheckoutDate(), savedHistoryRecord.getCheckoutDate());
        assertEquals(historyRecord1.getReturnDate(), savedHistoryRecord.getReturnDate());
        assertTrue(savedHistoryRecord.getId() > 0);
    }

    @Test
    void findByHistoryRecordHolder_shouldReturnAllHistoryRecordsByHistoryRecordHolderPaginated() {

        historyRecordRepository.save(historyRecord1);
        historyRecordRepository.save(historyRecord2);

        Page<HistoryRecord> historyRecords = historyRecordRepository.findByHistoryRecordHolder(historyRecordHolder, PageRequest.of(0, 5));

        assertNotNull(historyRecords);
        assertEquals(2, historyRecords.getTotalElements());
        assertEquals(historyRecord1, historyRecords.getContent().get(0));
        assertEquals(historyRecord2, historyRecords.getContent().get(1));
    }
}