package com.iliamalafeev.bookstore.bookstore_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "person_email")
    private String personEmail;

    @Column(name = "person_first_name")
    private String personFirstName;

    @Column(name = "person_last_name")
    private String personLastName;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    @JsonIgnore
    private Book reviewedBook;

    @Column(name = "date")
    @CreationTimestamp
    private LocalDateTime date;

    @NotNull(message = "Rating must be present")
    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_description")
    private String reviewDescription;

    public Review(String personEmail, String personFirstName, String personLastName, Book reviewedBook, LocalDateTime date, Double rating, String reviewDescription) {
        this.personEmail = personEmail;
        this.personFirstName = personFirstName;
        this.personLastName = personLastName;
        this.reviewedBook = reviewedBook;
        this.date = date;
        this.rating = rating;
        this.reviewDescription = reviewDescription;
    }
}