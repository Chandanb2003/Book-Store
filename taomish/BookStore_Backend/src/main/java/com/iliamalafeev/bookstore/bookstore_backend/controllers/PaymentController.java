package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.iliamalafeev.bookstore.bookstore_backend.dto.PaymentInfoDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.services.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:5173/")
@RestController
@RequestMapping("/api/payment/secure")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Payment Controller")
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtUtils jwtUtils;

    @Autowired
    public PaymentController(PaymentService paymentService, JwtUtils jwtUtils) {
        this.paymentService = paymentService;
        this.jwtUtils = jwtUtils;
    }

    private String extractEmail(String token) {
        String jwt = token.substring(7);
        return jwtUtils.extractPersonEmail(jwt);
    }

    @Operation(summary = "Get pending fee amount for authenticated user.",
            description = "Get pending fee amount for authenticated user. Returns a value of type Double.")
    @GetMapping
    public ResponseEntity<Double> findByPersonEmail(@RequestHeader("Authorization") String token) {

        Double responseBody = paymentService.findPaymentFeesByPersonEmail(extractEmail(token));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Create Stripe PaymentIntent for authenticated user.",
            description = "Create Stripe PaymentIntent for authenticated user. Returns a json formatted value of Stripe PaymentIntent object.")
    @PostMapping("/payment-intent")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentInfoDTO paymentInfoDTO) throws StripeException {

        PaymentIntent paymentIntent = paymentService.createPaymentIntent(paymentInfoDTO);
        String responseBody = paymentIntent.toJson();
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    @Operation(summary = "Confirm that payment attempt went successfully.",
            description = "Confirm that payment attempt went successfully. Updates a value of payment amount for authenticated user.")
    @PutMapping("/payment-complete")
    public ResponseEntity<HttpStatus> stripePaymentComplete(@RequestHeader("Authorization") String token) {

        paymentService.stripePayment(extractEmail(token));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
