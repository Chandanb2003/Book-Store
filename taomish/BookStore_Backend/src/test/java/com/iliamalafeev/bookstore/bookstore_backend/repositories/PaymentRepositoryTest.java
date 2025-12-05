package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Payment;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class PaymentRepositoryTest {

    private Person paymentHolder;
    private Payment payment;
    private final PersonRepository personRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    PaymentRepositoryTest(PersonRepository personRepository, PaymentRepository paymentRepository) {
        this.personRepository = personRepository;
        this.paymentRepository = paymentRepository;
    }

    @BeforeEach
    void setUp() {

        paymentHolder = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), "email@email.com", "Password");
        paymentHolder.setRole(Role.ROLE_USER);
        paymentHolder.setRegisteredAt(LocalDateTime.now());

        payment = new Payment(paymentHolder, 10.5);

        personRepository.save(paymentHolder);
    }

    @Test
    public void save_shouldSavePaymentToDatabase() {

        Payment savedPayment = paymentRepository.save(payment);

        assertNotNull(savedPayment);
        assertEquals(payment.getPaymentHolder(), savedPayment.getPaymentHolder());
        assertEquals(payment.getAmount(), savedPayment.getAmount());
        assertTrue(savedPayment.getId() > 0);
    }

    @Test
    void findByPaymentHolder_shouldReturnPaymentOptionalByPaymentHolder() {

        paymentRepository.save(payment);

        Optional<Payment> paymentOptional = paymentRepository.findByPaymentHolder(paymentHolder);

        assertTrue(paymentOptional.isPresent());
        assertEquals(payment.getPaymentHolder(), paymentOptional.get().getPaymentHolder());
        assertEquals(payment.getAmount(), paymentOptional.get().getAmount());
    }
}