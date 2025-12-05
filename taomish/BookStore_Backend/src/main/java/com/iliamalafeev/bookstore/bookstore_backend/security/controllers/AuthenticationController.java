package com.iliamalafeev.bookstore.bookstore_backend.security.controllers;

import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonLoginDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests.PersonRegistrationDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.dto.responses.AuthenticationResponse;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:5173/")
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register a new user.",
            description = "Creates a new user entity and adds it into a DataBase. Requires a valid PersonRegistrationDTO object as a request body. Returns a valid JWT token for new authenticated user.")
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody @Valid PersonRegistrationDTO personRegistrationDTO,
                                                           BindingResult bindingResult) {

        AuthenticationResponse responseBody = authenticationService.registerPerson(personRegistrationDTO, bindingResult);
        return new ResponseEntity<>(responseBody, HttpStatus.CREATED);
    }

    @Operation(summary = "Authenticate an existing user.",
            description = "Looks for a provided user credentials in a DataBase. Requires a valid PersonLoginDTO object as a request body. Returns a valid JWT token for authenticated user.")
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid PersonLoginDTO personLoginDTO,
                                                               BindingResult bindingResult) {

        AuthenticationResponse responseBody = authenticationService.authenticatePerson(personLoginDTO, bindingResult);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
