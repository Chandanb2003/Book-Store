package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Discussion;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class DiscussionRepositoryTest {

    private Person discussionHolder;
    private Discussion discussion1;
    private Discussion discussion2;
    private Discussion discussion3;

    private final DiscussionRepository discussionRepository;
    private final PersonRepository personRepository;

    @Autowired
    DiscussionRepositoryTest(DiscussionRepository repository, PersonRepository personRepository) {
        this.discussionRepository = repository;
        this.personRepository = personRepository;
    }

    @BeforeEach
    void setUp() {

        discussionHolder = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), "email@email.com", "Password");
        discussionHolder.setRole(Role.ROLE_USER);
        discussionHolder.setRegisteredAt(LocalDateTime.now());

        discussion1 = new Discussion(discussionHolder, "Title 1", "Question 1");
        discussion1.setAdminEmail(null);
        discussion1.setResponse(null);
        discussion1.setClosed(false);

        discussion2 = new Discussion(discussionHolder, "Title 2", "Question 2");
        discussion2.setAdminEmail(null);
        discussion2.setResponse(null);
        discussion2.setClosed(false);

        discussion3 = new Discussion(discussionHolder, "Title 3", "Question 3");
        discussion3.setAdminEmail("admin@email.com");
        discussion3.setResponse("response3");
        discussion3.setClosed(true);

        personRepository.save(discussionHolder);
    }

    @Test
    void save_shouldSaveDiscussionToDatabase() {

        Discussion savedDiscussion = discussionRepository.save(discussion1);

        assertNotNull(savedDiscussion);
        assertEquals(discussion1.getDiscussionHolder(), savedDiscussion.getDiscussionHolder());
        assertEquals(discussion1.getTitle(), savedDiscussion.getTitle());
        assertEquals(discussion1.getQuestion(), savedDiscussion.getQuestion());
        assertEquals(discussion1.getAdminEmail(), savedDiscussion.getAdminEmail());
        assertEquals(discussion1.getResponse(), savedDiscussion.getResponse());
        assertEquals(discussion1.getClosed(), savedDiscussion.getClosed());
        assertTrue(savedDiscussion.getId() > 0);
    }

    @Test
    void findById_shouldReturnDiscussionById() {

        Discussion savedDiscussion = discussionRepository.save(discussion2);

        Optional<Discussion> discussion = discussionRepository.findById(savedDiscussion.getId());

        assertNotNull(discussion);
        assertTrue(discussion.isPresent());
        assertEquals(discussion.get().getId(), savedDiscussion.getId());
    }

    @Test
    void findByDiscussionHolder_shouldReturnAllDiscussionsByDiscussionHolderPaginated() {

        Discussion savedDiscussion1 = discussionRepository.save(discussion1);
        Discussion savedDiscussion2 = discussionRepository.save(discussion2);
        Discussion savedDiscussion3 = discussionRepository.save(discussion3);

        Page<Discussion> discussions = discussionRepository.findByDiscussionHolder(discussionHolder, PageRequest.of(0, 5));

        assertNotNull(discussions);
        assertEquals(3, discussions.getTotalElements());
        assertEquals(savedDiscussion1, discussions.getContent().get(0));
        assertEquals(savedDiscussion2, discussions.getContent().get(1));
        assertEquals(savedDiscussion3, discussions.getContent().get(2));
    }

    @Test
    void findByClosed_shouldReturnAllDiscussionsByClosedTrueOrFalsePaginated() {

        Discussion savedDiscussion1 = discussionRepository.save(discussion1);
        Discussion savedDiscussion2 = discussionRepository.save(discussion2);
        Discussion savedDiscussion3 = discussionRepository.save(discussion3);

        Page<Discussion> openDiscussions = discussionRepository.findByClosed(false, PageRequest.of(0, 5));
        Page<Discussion> closedDiscussions = discussionRepository.findByClosed(true, PageRequest.of(0, 5));

        assertNotNull(openDiscussions);
        assertNotNull(closedDiscussions);
        assertEquals(2, openDiscussions.getTotalElements());
        assertEquals(1, closedDiscussions.getTotalElements());
        assertEquals(savedDiscussion1, openDiscussions.getContent().get(0));
        assertEquals(savedDiscussion2, openDiscussions.getContent().get(1));
        assertEquals(savedDiscussion3, closedDiscussions.getContent().get(0));
    }
}