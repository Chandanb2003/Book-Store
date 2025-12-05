package com.iliamalafeev.bookstore.bookstore_backend.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "discussion")
public class Discussion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "person_email", referencedColumnName = "email")
    @JsonIgnore
    private Person discussionHolder;

    @NotBlank(message = "Discussion title must be present and contain at least 1 character")
    @Size(max = 100, message = "Discussion title length must not exceed 100 characters")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Question must be present and contain at least 1 character")
    @Column(name = "question")
    private String question;

    @Column(name = "admin_email")
    private String adminEmail;

    @Column(name = "response")
    private String response;

    @Column(name = "closed")
    private Boolean closed;

    public Discussion(Person discussionHolder, String title, String question) {
        this.discussionHolder = discussionHolder;
        this.title = title;
        this.question = question;
    }
}
