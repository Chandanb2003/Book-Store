package com.iliamalafeev.bookstore.bookstore_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "history_record")
public class HistoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_email", referencedColumnName = "email")
    @JsonIgnore
    private Person historyRecordHolder;

    @ManyToOne
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    @JsonIgnore
    private Book historyRecordedBook;

    @Column(name = "checkout_date")
    private LocalDate checkoutDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    public HistoryRecord(Person historyRecordHolder, Book historyRecordedBook, LocalDate checkoutDate, LocalDate returnDate) {
        this.historyRecordHolder = historyRecordHolder;
        this.historyRecordedBook = historyRecordedBook;
        this.checkoutDate = checkoutDate;
        this.returnDate = returnDate;
    }
}
