package com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonRegistrationDTO {

    @NotBlank(message = "First name must contain at least 1 character")
    @Size(max = 100, message = "First name length must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name must contain at least 1 character")
    @Size(max = 100, message = "Last name length must not exceed 100 characters")
    private String lastName;

    @NotNull(message = "Date cannot be left blank")
    @Past(message = "Birth date cannot be in the future")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email must contain at least 1 character")
    @Size(max = 100, message = "Email length must not exceed 100 characters")
    @Email(message = "This field must be formatted as Email address")
    private String email;

    @NotBlank(message = "Password must contain at least 10 characters")
    @Size(min = 10, max = 30, message = "Password length must be 10 - 30 characters")
    private String password;
}
