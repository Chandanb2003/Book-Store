package com.iliamalafeev.bookstore.bookstore_backend.security.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonLoginDTO {

    @NotBlank(message = "Email must contain at least 1 character")
    @Size(max = 100, message = "Email length must not exceed 100 characters")
    @Email(message = "This field must be formatted as Email address")
    private String email;

    @NotBlank(message = "Password must contain at least 10 characters")
    @Size(min = 10, max = 30, message = "Password length must be 10 - 30 characters")
    private String password;
}
