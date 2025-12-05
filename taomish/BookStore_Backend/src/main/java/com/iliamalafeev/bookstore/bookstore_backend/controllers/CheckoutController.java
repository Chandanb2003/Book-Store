package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.iliamalafeev.bookstore.bookstore_backend.dto.CheckoutDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.services.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("http://localhost:5173/")
@RestController
@RequestMapping("api/checkouts/secure")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Checkout Controller")
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final JwtUtils jwtUtils;

    @Autowired
    public CheckoutController(CheckoutService checkoutService, JwtUtils jwtUtils) {
        this.checkoutService = checkoutService;
        this.jwtUtils = jwtUtils;
    }

    private String extractEmail(String token) {
        String jwt = token.substring(7);
        return jwtUtils.extractPersonEmail(jwt);
    }

    @Operation(summary = "Get a number of current checkouts held by authenticated user.",
            description = "Returns a number of current checkouts as an int.")
    @GetMapping("/current-loans-count")
    public ResponseEntity<Integer> getCurrentCheckoutsCount(@RequestHeader(value = "Authorization") String token) {

        Integer responseBody = checkoutService.getCurrentCheckoutsCount(extractEmail(token));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Get a list of all current checkouts held by authenticated user.",
            description = "Returns a list containing CheckoutDTO objects.")
    @GetMapping("/current-checkouts")
    public ResponseEntity<List<CheckoutDTO>> getCurrentCheckouts(@RequestHeader(value = "Authorization") String token) {

        List<CheckoutDTO> responseBody = checkoutService.getCurrentCheckouts(extractEmail(token));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
