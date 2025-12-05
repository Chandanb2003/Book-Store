package com.iliamalafeev.bookstore.bookstore_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "person_email", referencedColumnName = "email")
    @JsonIgnoreProperties("payment")
    private Person paymentHolder;

    @Column(name = "amount")
    private Double amount;

    public Payment(Person paymentHolder, Double amount) {
        this.paymentHolder = paymentHolder;
        this.amount = amount;
    }
}
