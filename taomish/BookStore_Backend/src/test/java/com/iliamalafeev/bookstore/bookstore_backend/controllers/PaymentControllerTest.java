package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.PaymentInfoDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.PaymentService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PaymentException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@WithMockUser
class PaymentControllerTest {

    private final String jwtToken = "TestJWT";
    private final String personEmail = "email@email.com";
    private final String baseURL = "/api/payment/secure";

    private PaymentInfoDTO paymentInfoDTO;
    private PaymentIntent paymentIntent;

    @MockBean private PaymentService paymentService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    PaymentControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        paymentInfoDTO = new PaymentInfoDTO();
        paymentInfoDTO.setAmount(1000);
        paymentInfoDTO.setCurrency("EUR");
        paymentInfoDTO.setReceiptEmail(personEmail);

        paymentIntent = new PaymentIntent();
        paymentIntent.setAmount((long) paymentInfoDTO.getAmount());
        paymentIntent.setCurrency(paymentInfoDTO.getCurrency());
        paymentIntent.setReceiptEmail(personEmail);
    }

    @Test
    void findByPersonEmail_shouldReturnPaymentFeesForAuthenticatedPerson() throws Exception {

        Double paymentFees = 10.00;

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(paymentService.findPaymentFeesByPersonEmail(any(String.class))).thenReturn(paymentFees);

        mockMvc.perform(get(baseURL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(paymentFees)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(paymentService, times(1)).findPaymentFeesByPersonEmail(any(String.class));
    }

    @Test
    void findByPersonEmail_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);
        String invalidPersonEmail = "invalidEmail@email.com";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(paymentService.findPaymentFeesByPersonEmail(any(String.class))).thenThrow(exception);

        mockMvc.perform(get(baseURL)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(paymentService, times(1)).findPaymentFeesByPersonEmail(any(String.class));
    }

    @Test
    void createPaymentIntent_shouldCreateValidStripePaymentIntent() throws Exception {

        String url = baseURL + "/payment-intent";
        String dtoJson = objectMapper.writeValueAsString(paymentInfoDTO);

        when(paymentService.createPaymentIntent(any(PaymentInfoDTO.class))).thenReturn(paymentIntent);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dtoJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(paymentInfoDTO.getAmount()))
                .andExpect(jsonPath("$.currency").value(paymentInfoDTO.getCurrency()))
                .andExpect(jsonPath("$.receipt_email").value(paymentInfoDTO.getReceiptEmail()));

        verify(paymentService, times(1)).createPaymentIntent(any(PaymentInfoDTO.class));
    }

    @Test
    void stripePaymentComplete_shouldUpdatePaymentInfoForAuthenticatedPerson() throws Exception {

        String url = baseURL + "/payment-complete";

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doNothing().when(paymentService).stripePayment(any(String.class));

        mockMvc.perform(put(url)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(paymentService, times(1)).stripePayment(any(String.class));
    }

    @Test
    void stripePaymentComplete_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/payment-complete";

        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(paymentService).stripePayment(any(String.class));

        mockMvc.perform(put(url)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(paymentService, times(1)).stripePayment(any(String.class));
    }

    @Test
    void stripePaymentComplete_shouldReturnNotFoundIfPaymentInfoDoesNotExist() throws Exception {

        String url = baseURL + "//payment-complete";

        PaymentException exception = new PaymentException("Payment information is missing", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        doThrow(exception).when(paymentService).stripePayment(any(String.class));

        mockMvc.perform(put(url)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Payment information is missing"));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(paymentService, times(1)).stripePayment(any(String.class));
    }
}