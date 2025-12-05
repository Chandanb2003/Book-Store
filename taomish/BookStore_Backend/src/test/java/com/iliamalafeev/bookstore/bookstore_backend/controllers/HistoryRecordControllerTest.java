package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.HistoryRecordDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.HistoryRecordService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HistoryRecordController.class)
@WithMockUser
class HistoryRecordControllerTest {

    private final String jwtToken = "TestJWT";
    private final int page = 0;
    private final int recordsPerPage = 5;
    private final String baseURL = "/api/history-records/secure";

    private HistoryRecordDTO historyRecordDTO1;
    private HistoryRecordDTO historyRecordDTO2;

    @MockBean private HistoryRecordService historyRecordService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    HistoryRecordControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setDescription("Genre");

        BookDTO bookDTO1 = new BookDTO();
        bookDTO1.setId(1L);
        bookDTO1.setTitle("Title 1");
        bookDTO1.setAuthor("Author 1");
        bookDTO1.setDescription("Description 1");
        bookDTO1.setCopies(10);
        bookDTO1.setCopiesAvailable(10);
        bookDTO1.setImg("encodedImage 1");
        bookDTO1.setGenres(List.of(genreDTO));

        BookDTO bookDTO2 = new BookDTO();
        bookDTO1.setId(2L);
        bookDTO2.setTitle("Title 1");
        bookDTO2.setAuthor("Author 1");
        bookDTO2.setDescription("Description 1");
        bookDTO2.setCopies(10);
        bookDTO2.setCopiesAvailable(10);
        bookDTO2.setImg("encodedImage 1");
        bookDTO2.setGenres(List.of(genreDTO));

        historyRecordDTO1 = new HistoryRecordDTO();
        historyRecordDTO1.setBookDTO(bookDTO1);
        historyRecordDTO1.setCheckoutDate(LocalDate.now().minusDays(10));
        historyRecordDTO1.setReturnDate(LocalDate.now().minusDays(3));

        historyRecordDTO2 = new HistoryRecordDTO();
        historyRecordDTO2.setBookDTO(bookDTO2);
        historyRecordDTO2.setCheckoutDate(LocalDate.now().minusDays(10));
        historyRecordDTO2.setReturnDate(LocalDate.now().minusDays(3));
    }

    @Test
    void findAllByPersonEmail_shouldReturnAllHistoryRecordsByPersonEmail() throws Exception {

        List<HistoryRecordDTO> pageContent = List.of(historyRecordDTO1, historyRecordDTO2);
        Pageable pageable = PageRequest.of(page, recordsPerPage);
        Page<HistoryRecordDTO> historyRecordDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        String personEmail = "email@email.com";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(historyRecordService.findAllByPersonEmail(any(String.class), any(Pageable.class))).thenReturn(historyRecordDTOPage);

        mockMvc.perform(get(baseURL)
                        .param("page", String.valueOf(page))
                        .param("records-per-page", String.valueOf(recordsPerPage))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(historyRecordDTOPage)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(historyRecordService, times(1)).findAllByPersonEmail(any(String.class), any(Pageable.class));
    }

    @Test
    void findAllByPersonEmail_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        String invalidPersonEmail = "invalidEmail@email.com";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(historyRecordService.findAllByPersonEmail(any(String.class), any(Pageable.class))).thenThrow(exception);

        mockMvc.perform(get(baseURL)
                        .param("page", String.valueOf(page))
                        .param("records-per-page", String.valueOf(recordsPerPage))
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(historyRecordService, times(1)).findAllByPersonEmail(any(String.class), any(Pageable.class));
    }
}