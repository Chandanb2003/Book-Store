package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Discussion;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

    Page<Discussion> findByDiscussionHolder(Person person, Pageable pageable);

    Page<Discussion> findByClosed(boolean isClosed, Pageable pageable);
}
