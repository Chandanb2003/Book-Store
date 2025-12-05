package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.HistoryRecordDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.entities.HistoryRecord;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.HistoryRecordRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryRecordServiceTest {

    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";
    private final int page = 0;
    private final int recordsPerPage = 5;

    private Person person;
    private HistoryRecord historyRecord1;
    private HistoryRecord historyRecord2;
    private BookDTO bookDTO;
    private HistoryRecordDTO historyRecordDTO;

    @Mock private ModelMapper modelMapper;
    @Mock private HistoryRecordRepository historyRecordRepository;
    @Mock private PersonRepository personRepository;

    @InjectMocks
    private HistoryRecordService historyRecordService;

    @BeforeEach
    void setUp() {

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());

        Book book1 = new Book("Title 1", "Author 1", "Description 1", 10, 10, "encodedImage 1");
        book1.setGenres(List.of(new Genre("Genre 1")));

        Book book2 = new Book("Title 2", "Author 2", "Description 2", 10, 10, "encodedImage 2");
        book2.setGenres(List.of(new Genre("Genre 2")));

        historyRecord1 = new HistoryRecord(person, book1, LocalDate.now().minusDays(2), LocalDate.now());
        historyRecord2 = new HistoryRecord(person, book2, LocalDate.now().minusDays(2), LocalDate.now());

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setDescription("Genre");

        bookDTO = new BookDTO();
        bookDTO.setTitle("Title");
        bookDTO.setAuthor("Author");
        bookDTO.setDescription("Description");
        bookDTO.setCopies(10);
        bookDTO.setCopiesAvailable(10);
        bookDTO.setImg("encodedImage");
        bookDTO.setGenres(List.of(genreDTO));

        historyRecordDTO = new HistoryRecordDTO();
        historyRecordDTO.setBookDTO(bookDTO);
        historyRecordDTO.setCheckoutDate(LocalDate.now().minusDays(2));
        historyRecordDTO.setReturnDate(LocalDate.now());
    }

    @Test
    void findAllByPersonEmail_shouldReturnAllHistoryRecordsForAuthenticatedPerson() {

        List<HistoryRecord> pageContent = List.of(historyRecord1, historyRecord2);
        Pageable pageable = PageRequest.of(page, recordsPerPage);
        Page<HistoryRecord> historyRecordsPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(historyRecordRepository.findByHistoryRecordHolder(any(Person.class), any(Pageable.class))).thenReturn(historyRecordsPage);
        when(modelMapper.map(any(HistoryRecord.class), eq(HistoryRecordDTO.class))).thenReturn(historyRecordDTO);
        when(modelMapper.map(any(Book.class), eq(BookDTO.class))).thenReturn(bookDTO);

        Page<HistoryRecordDTO> historyRecordDTOs = historyRecordService.findAllByPersonEmail(personEmail, pageable);

        assertNotNull(historyRecordDTOs);
        assertEquals(pageContent.size(), historyRecordDTOs.getContent().size());
        assertEquals(bookDTO, historyRecordDTOs.getContent().get(0).getBookDTO());
        assertEquals(bookDTO, historyRecordDTOs.getContent().get(1).getBookDTO());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(historyRecordRepository, times(1)).findByHistoryRecordHolder(any(Person.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(HistoryRecord.class), eq(HistoryRecordDTO.class));
        verify(modelMapper, times(pageContent.size())).map(any(Book.class), eq(BookDTO.class));
    }

    @Test
    void findAllByPersonEmail_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        Pageable pageable = PageRequest.of(page, recordsPerPage);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> historyRecordService.findAllByPersonEmail(invalidPersonEmail, pageable));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(historyRecordRepository, times(0)).findByHistoryRecordHolder(any(Person.class), any(Pageable.class));
        verify(modelMapper, times(0)).map(any(HistoryRecord.class), eq(HistoryRecordDTO.class));
        verify(modelMapper, times(0)).map(any(Book.class), eq(BookDTO.class));
    }
}