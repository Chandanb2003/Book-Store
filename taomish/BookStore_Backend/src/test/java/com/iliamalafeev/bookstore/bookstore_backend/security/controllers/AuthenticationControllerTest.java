package com.iliamalafeev.bookstore.bookstore_backend.security.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonRegistrationDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
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
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@WithMockUser
class AuthenticationControllerTest {

    private final String jwtToken = "TestJWT";
    private final String baseURL = "/api/auth";

    private PersonRegistrationDTO personRegistrationDTO;
    private PersonLoginDTO personLoginDTO;

    @MockBean private AuthenticationService authenticationService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    AuthenticationControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        String personEmail = "email@email.com";

        personRegistrationDTO = new PersonRegistrationDTO();
        personRegistrationDTO.setEmail(personEmail);
        personRegistrationDTO.setPassword("Password");
        personRegistrationDTO.setFirstName("First Name");
        personRegistrationDTO.setLastName("Last Name");
        personRegistrationDTO.setDateOfBirth(LocalDate.of(1990, 1, 1));

        personLoginDTO = new PersonLoginDTO();
        personLoginDTO.setEmail(personEmail);
        personLoginDTO.setPassword("Password");
    }

    @Test
    void register_shouldRegisterPersonAndReturnAuthenticationResponseWithJWT() throws Exception {

        String url = baseURL + "/register";
        AuthenticationResponse response = new AuthenticationResponse(jwtToken);

        when(authenticationService.registerPerson(any(PersonRegistrationDTO.class), any(BindingResult.class))).thenReturn(response);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(personRegistrationDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(authenticationService, times(1)).registerPerson(any(PersonRegistrationDTO.class), any(BindingResult.class));
    }

    @Test
    void register_shouldReturnForbiddenIfPersonRegistrationDtoIsInvalid() throws Exception {

        String url = baseURL + "/register";
        PersonException exception = new PersonException("Some fields are invalid. email: Person with this email is already registered; ", HttpStatus.FORBIDDEN);

        when(authenticationService.registerPerson(any(PersonRegistrationDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(personRegistrationDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Some fields are invalid. email: Person with this email is already registered; "));

        verify(authenticationService, times(1)).registerPerson(any(PersonRegistrationDTO.class), any(BindingResult.class));
    }

    @Test
    void authenticate_shouldCheckCredentialsAndReturnAuthenticationResponseWithJWT() throws Exception {

        String url = baseURL + "/authenticate";
        AuthenticationResponse response = new AuthenticationResponse(jwtToken);

        when(authenticationService.authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class))).thenReturn(response);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(personLoginDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void authenticate_shouldReturnForbiddenIfPersonLoginDtoIsInvalid() throws Exception {

        String url = baseURL + "/authenticate";
        PersonException exception = new PersonException("Some fields are invalid. email: This field must be formatted as Email address; ", HttpStatus.FORBIDDEN);

        when(authenticationService.authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(personLoginDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Some fields are invalid. email: This field must be formatted as Email address; "));

        verify(authenticationService, times(1)).authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class));
    }

    @Test
    void authenticate_shouldReturnForbiddenIfCredentialsAreInvalid() throws Exception {

        String url = baseURL + "/authenticate";
        PersonException exception = new PersonException("Login or password is incorrect. ", HttpStatus.FORBIDDEN);

        when(authenticationService.authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(personLoginDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Login or password is incorrect. "));

        verify(authenticationService, times(1)).authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class));
    }

    @Test
    void authenticate_shouldReturnNotFoundIfPersonWithSuchEmailDoesNotExist() throws Exception {

        String url = baseURL + "/authenticate";
        PersonException exception = new PersonException("Person with such email is not found. Please check the input fields. ", HttpStatus.NOT_FOUND);

        when(authenticationService.authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(personLoginDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. Please check the input fields. "));

        verify(authenticationService, times(1)).authenticatePerson(any(PersonLoginDTO.class), any(BindingResult.class));
    }
}