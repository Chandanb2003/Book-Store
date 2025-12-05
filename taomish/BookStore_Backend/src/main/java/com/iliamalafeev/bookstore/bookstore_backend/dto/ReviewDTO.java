package com.iliamalafeev.bookstore.bookstore_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewDTO {

    private Long id;

    private String personEmail;

    private String personFirstName;

    private LocalDateTime date;

    @NotNull(message = "Rating must be present")
    private Double rating;

    private String reviewDescription;
}
