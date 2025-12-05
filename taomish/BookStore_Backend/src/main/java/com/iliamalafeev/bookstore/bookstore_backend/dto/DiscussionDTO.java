package com.iliamalafeev.bookstore.bookstore_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscussionDTO {

    private Long id;

    private String personEmail;

    private String personFirstName;

    private String personLastName;

    @NotBlank(message = "Discussion title must be present and contain at least 1 character")
    @Size(max = 100, message = "Discussion title length must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Question must be present and contain at least 1 character")
    private String question;

    private String adminEmail;

    private String response;

    private Boolean closed;
}
