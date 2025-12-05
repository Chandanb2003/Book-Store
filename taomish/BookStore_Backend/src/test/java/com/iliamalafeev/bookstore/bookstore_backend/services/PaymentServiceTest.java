package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.PaymentInfoDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Payment;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PaymentRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PaymentException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";

    private Person person;
    private Payment payment;
    private PaymentInfoDTO paymentInfoDTO;
    private PaymentIntent paymentIntent;

    @Mock private PaymentRepository paymentRepository;
    @Mock private PersonRepository personRepository;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());

        payment = new Payment(person, 00.00);

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
    void findPaymentFeesByPersonEmail_shouldReturnPaymentFeesForAuthenticatedPerson() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.of(payment));

        Double returnedAmount = assertDoesNotThrow(() -> paymentService.findPaymentFeesByPersonEmail(personEmail));

        assertNotNull(returnedAmount);
        assertEquals(payment.getAmount(), returnedAmount);
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
    }

    @Test
    void findPaymentFeesByPersonEmail_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> paymentService.findPaymentFeesByPersonEmail(invalidPersonEmail));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
    }

    @Test
    void findPaymentFeesByPersonEmail_shouldReturnZeroIfPaymentInfoDoesNotExist() {

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.empty());

        Double returnedAmount = assertDoesNotThrow(() -> paymentService.findPaymentFeesByPersonEmail(personEmail));

        assertNotNull(returnedAmount);
        assertEquals(00.00, returnedAmount);
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
    }

    @Test
    void createPaymentIntent_shouldCreateValidStripePaymentIntent() {

        try (MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)) {

            paymentIntentMock.when(() -> PaymentIntent.create(anyMap())).thenReturn(paymentIntent);

            PaymentIntent returnedPaymentIntent = assertDoesNotThrow(() -> paymentService.createPaymentIntent(paymentInfoDTO));

            assertNotNull(returnedPaymentIntent);
            assertEquals(paymentIntent.getAmount(), returnedPaymentIntent.getAmount());
            assertEquals(paymentIntent.getCurrency(), returnedPaymentIntent.getCurrency());
            assertEquals(paymentIntent.getReceiptEmail(), returnedPaymentIntent.getReceiptEmail());
            paymentIntentMock.verify(() -> PaymentIntent.create(anyMap()), times(1));
        }
    }

    @Test
    void stripePayment_shouldUpdatePaymentInfoForAuthenticatedPerson() {

        payment.setAmount(10.00);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        assertDoesNotThrow(() -> paymentService.stripePayment(personEmail));
        assertEquals(00.00, payment.getAmount());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void stripePayment_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        payment.setAmount(10.00);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> paymentService.stripePayment(invalidPersonEmail));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(10.00, payment.getAmount());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(paymentRepository, times(0)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
    }

    @Test
    void stripePayment_shouldThrowPaymentExceptionIfPaymentInfoDoesNotExist() {

        payment.setAmount(10.00);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(paymentRepository.findByPaymentHolder(any(Person.class))).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () -> paymentService.stripePayment(personEmail));

        assertEquals("Payment information is missing", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals(10.00, payment.getAmount());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(paymentRepository, times(1)).findByPaymentHolder(any(Person.class));
        verify(paymentRepository, times(0)).save(any(Payment.class));
    }
}