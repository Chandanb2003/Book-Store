package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.CheckoutDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.CheckoutService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutController.class)
@WithMockUser
class CheckoutControllerTest {

    private final String jwtToken = "TestJWT";
    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";
    private final String baseURL = "/api/checkouts/secure";

    private CheckoutDTO checkoutDTO1;
    private CheckoutDTO checkoutDTO2;

    @MockBean private CheckoutService checkoutService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    CheckoutControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setDescription("Genre 1");

        BookDTO bookDTO1 = new BookDTO();
        bookDTO1.setTitle("Title 1");
        bookDTO1.setAuthor("Author 1");
        bookDTO1.setDescription("Description 1");
        bookDTO1.setCopies(10);
        bookDTO1.setCopiesAvailable(10);
        bookDTO1.setImg("encodedImage 1");
        bookDTO1.setGenres(List.of(genreDTO));

        BookDTO bookDTO2 = new BookDTO();
        bookDTO2.setTitle("Title 2");
        bookDTO2.setAuthor("Author 2");
        bookDTO2.setDescription("Description 2");
        bookDTO2.setCopies(10);
        bookDTO2.setCopiesAvailable(10);
        bookDTO2.setImg("encodedImage 2");
        bookDTO2.setGenres(List.of(genreDTO));

        checkoutDTO1 = new CheckoutDTO();
        checkoutDTO1.setBookDTO(bookDTO1);
        checkoutDTO1.setDaysLeft(7);

        checkoutDTO2 = new CheckoutDTO();
        checkoutDTO2.setBookDTO(bookDTO2);
        checkoutDTO2.setDaysLeft(7);
    }

    @Test
    void getCurrentCheckoutsCount_shouldReturnCountOfCheckoutsMadeByPerson() throws Exception {

        String url = baseURL + "/current-loans-count";
        Integer checkoutsCount = 2;

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(checkoutService.getCurrentCheckoutsCount(any(String.class))).thenReturn(checkoutsCount);

        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(checkoutsCount)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(checkoutService, times(1)).getCurrentCheckoutsCount(any(String.class));
    }

    @Test
    void getCurrentCheckoutsCount_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/current-loans-count";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(checkoutService.getCurrentCheckoutsCount(any(String.class))).thenThrow(exception);

        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(checkoutService, times(1)).getCurrentCheckoutsCount(any(String.class));
    }

    @Test
    void getCurrentCheckouts_shouldReturnAllCurrentCheckoutsMadeByPerson() throws Exception {

        String url = baseURL + "/current-checkouts";
        List<CheckoutDTO> checkoutDTOList = List.of(checkoutDTO1, checkoutDTO2);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(checkoutService.getCurrentCheckouts(any(String.class))).thenReturn(checkoutDTOList);

        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(checkoutDTOList)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(checkoutService, times(1)).getCurrentCheckouts(any(String.class));
    }

    @Test
    void getCurrentCheckouts_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/current-checkouts";
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(checkoutService.getCurrentCheckouts(any(String.class))).thenThrow(exception);

        mockMvc.perform(get(url)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(checkoutService, times(1)).getCurrentCheckouts(any(String.class));
    }
}