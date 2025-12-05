package com.iliamalafeev.bookstore.bookstore_backend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookDTO {

    private Long id;

    @NotBlank(message = "Book title must be present and contain at least 1 character")
    @Size(max = 100, message = "Book title length must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Author name must be present and contain at least 1 character")
    @Size(max = 100, message = "Author name length must not exceed 100 characters")
    private String author;

    @NotBlank(message = "Book description must contain at least 1 character")
    private String description;

    @NotNull(message = "Copies count must not be null")
    @Min(value = 1, message = "Copies count cannot be below 1")
    private Integer copies;

    @NotNull(message = "Available copies count must not be null")
    @Min(value = 0, message = "Available copies count cannot be below 0")
    private Integer copiesAvailable;

    @NotBlank(message = "Cover image must be present")
    private String img;

    @NotEmpty(message = "At least one genre must be assigned")
    private List<GenreDTO> genres;
}
