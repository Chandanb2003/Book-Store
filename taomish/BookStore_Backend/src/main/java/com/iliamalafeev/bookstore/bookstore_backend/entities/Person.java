package com.iliamalafeev.bookstore.bookstore_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "person")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "First name must contain at least 1 character")
    @Size(max = 100, message = "First name length must not exceed 100 characters")
    @Column(name = "first_name")
    private String firstName;

    @NotBlank(message = "Last name must contain at least 1 character")
    @Size(max = 100, message = "Last name length must not exceed 100 characters")
    @Column(name = "last_name")
    private String lastName;

    @NotNull(message = "Date cannot be left blank")
    @Past(message = "Birth date cannot be in the future")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email must contain at least 1 character")
    @Size(max = 100, message = "Email length must not exceed 100 characters")
    @Email(message = "This field must be formatted as Email address")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "checkoutHolder", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Checkout> checkouts;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "discussionHolder", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Discussion> discussions;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "historyRecordHolder", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HistoryRecord> historyRecords;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToOne(mappedBy = "paymentHolder", fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment payment;

    public Person(String firstName, String lastName, LocalDate dateOfBirth, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
    }
}
