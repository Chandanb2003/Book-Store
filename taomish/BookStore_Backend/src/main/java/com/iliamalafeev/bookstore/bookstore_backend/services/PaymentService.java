package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.PaymentInfoDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Payment;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PaymentRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.utils.ErrorsUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PersonRepository personRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, @Value("${stripe.key.secret}") String secretKey, PersonRepository personRepository) {
        this.paymentRepository = paymentRepository;
        this.personRepository = personRepository;
        Stripe.apiKey = secretKey;
    }

//  <------------------------------------------------------------------------------->
//  <-------------------- Service public methods for controller -------------------->
//  <------------------------------------------------------------------------------->

    public Double findPaymentFeesByPersonEmail(String personEmail) {

        Person person = getPersonFromRepository(personEmail);
        Optional<Payment> payment = getPaymentOptionalFromRepository(person);

        if (payment.isEmpty()) {
            return 0.0;
        }

        return payment.get().getAmount();
    }

    public PaymentIntent createPaymentIntent(PaymentInfoDTO paymentInfoDTO) throws StripeException {

        List<String> paymentMethodTypes = new ArrayList<>();
        paymentMethodTypes.add("card");

        Map<String, Object> params = new HashMap<>();
        params.put("amount", paymentInfoDTO.getAmount());
        params.put("currency", paymentInfoDTO.getCurrency());
        params.put("payment_method_types", paymentMethodTypes);

        return PaymentIntent.create(params);
    }

    @Transactional
    public void stripePayment(String personEmail) {

        Person person = getPersonFromRepository(personEmail);
        Optional<Payment> paymentOptional = getPaymentOptionalFromRepository(person);

        if (paymentOptional.isEmpty()) {
            ErrorsUtil.returnPaymentError("Payment information is missing", HttpStatus.NOT_FOUND);
        }

        Payment payment = paymentOptional.get();
        payment.setAmount(00.00);

        paymentRepository.save(payment);
    }

//  <-------------------------------------------------------------------------------------------->
//  <-------------------- Service private methods for some code re-usability -------------------->
//  <-------------------------------------------------------------------------------------------->

    private Person getPersonFromRepository(String personEmail) {

        Optional<Person> person = personRepository.findByEmail(personEmail);

        if (person.isEmpty()) {
            ErrorsUtil.returnPersonError("Person with such email is not found.", null, HttpStatus.NOT_FOUND);
        }

        return person.get();
    }

    private Optional<Payment> getPaymentOptionalFromRepository(Person person) {

        return paymentRepository.findByPaymentHolder(person);
    }
}