package com.iliamalafeev.bookstore.bookstore_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "checkout")
public class Checkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_email", referencedColumnName = "email")
    @JsonIgnore
    private Person checkoutHolder;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    @JsonIgnoreProperties("checkouts, historyRecords, reviews")
    private Book checkedOutBook;

    @Column(name = "checkout_date")
    private LocalDate checkoutDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    public Checkout(Person checkoutHolder, Book checkedOutBook, LocalDate checkoutDate, LocalDate returnDate) {
        this.checkoutHolder = checkoutHolder;
        this.checkedOutBook = checkedOutBook;
        this.checkoutDate = checkoutDate;
        this.returnDate = returnDate;
    }
}
