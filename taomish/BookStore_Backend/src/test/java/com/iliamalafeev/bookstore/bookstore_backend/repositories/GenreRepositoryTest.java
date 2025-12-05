package com.iliamalafeev.bookstore.bookstore_backend.repositories;

import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Sql("/schema.sql")
class GenreRepositoryTest {

    private Genre genre1;
    private Genre genre2;
    private Genre genre3;

    private final GenreRepository genreRepository;

    @Autowired
    GenreRepositoryTest(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @BeforeEach
    void setUp() {

        genre1 = new Genre("Genre 1");
        genre2 = new Genre("Genre 2");
        genre3 = new Genre("Genre 3");
    }

    @Test
    public void save_shouldSaveGenreToDatabase() {

        Genre savedGenre = genreRepository.save(genre1);

        assertNotNull(savedGenre);
        assertEquals(genre1.getDescription(), savedGenre.getDescription());
        assertTrue(savedGenre.getId() > 0);
    }

    @Test
    public void findAll_shouldReturnAllGenres() {

        Genre savedGenre1 = genreRepository.save(genre1);
        Genre savedGenre2 = genreRepository.save(genre2);

        List<Genre> allGenres = genreRepository.findAll();

        assertNotNull(allGenres);
        assertTrue(allGenres.contains(savedGenre1));
        assertTrue(allGenres.contains(savedGenre2));
        assertEquals(genre1.getDescription(), allGenres.get(0).getDescription());
        assertEquals(genre2.getDescription(), allGenres.get(1).getDescription());
        assertEquals(allGenres.size(), 2);
    }

    @Test
    void findByDescription_shouldReturnGenreOptionalByDescription() {

        genreRepository.save(genre1);

        Optional<Genre> genreOptional = genreRepository.findByDescription("Genre 1");

        assertNotNull(genreOptional);
        assertTrue(genreOptional.isPresent());
        assertEquals(genre1.getDescription(), genreOptional.get().getDescription());
        assertNotEquals(genre2.getDescription(), genreOptional.get().getDescription());
    }

    @Test
    void findByDescription_shouldReturnEmptyGenreOptionalIfGenreNotFoundByDescription() {

        genreRepository.save(genre1);

        Optional<Genre> genreOptional = genreRepository.findByDescription("Incorrect Genre");

        assertNotNull(genreOptional);
        assertTrue(genreOptional.isEmpty());
    }

    @Test
    void findByDescriptionIn_shouldReturnGenresListByDescriptionIn() {

        Genre savedGenre1 = genreRepository.save(genre1);
        Genre savedGenre2 = genreRepository.save(genre2);
        Genre savedGenre3 = genreRepository.save(genre3);

        List<String> descriptions = new ArrayList<>();
        descriptions.add("Genre 1");
        descriptions.add("Genre 2");

        List<Genre> genresByDescription = genreRepository.findByDescriptionIn(descriptions);

        assertNotNull(genresByDescription);
        assertEquals(genresByDescription.size(), 2);
        assertTrue(genresByDescription.contains(savedGenre1));
        assertTrue(genresByDescription.contains(savedGenre2));
        assertFalse(genresByDescription.contains(savedGenre3));
    }

    @Test
    void findByDescriptionIn_shouldNotReturnExtraGenres() {

        Genre savedGenre1 = genreRepository.save(genre1);
        Genre savedGenre2 = genreRepository.save(genre2);

        List<String> descriptions = new ArrayList<>();
        descriptions.add("Genre 1");
        descriptions.add("Genre 2");
        descriptions.add("Incorrect Genre");

        List<Genre> genresByDescription = genreRepository.findByDescriptionIn(descriptions);

        assertNotNull(genresByDescription);
        assertEquals(genresByDescription.size(), 2);
        assertTrue(genresByDescription.contains(savedGenre1));
        assertTrue(genresByDescription.contains(savedGenre2));
    }

    @Test
    void findByDescriptionIn_shouldReturnEmptyGenresListIfGenreNotFoundByDescription() {

        Genre savedGenre1 = genreRepository.save(genre1);
        Genre savedGenre2 = genreRepository.save(genre2);
        Genre savedGenre3 = genreRepository.save(genre3);

        List<String> descriptions = new ArrayList<>();
        descriptions.add("Incorrect Genre");

        List<Genre> genresByDescription = genreRepository.findByDescriptionIn(descriptions);

        assertNotNull(genresByDescription);
        assertEquals(genresByDescription.size(), 0);
        assertFalse(genresByDescription.contains(savedGenre1));
        assertFalse(genresByDescription.contains(savedGenre2));
        assertFalse(genresByDescription.contains(savedGenre3));
    }
}