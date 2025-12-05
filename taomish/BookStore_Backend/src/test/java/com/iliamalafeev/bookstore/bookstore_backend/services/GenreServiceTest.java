package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock private ModelMapper modelMapper;
    @Mock private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    @Test
    public void findAll_shouldReturnAllGenres() {

        List<Genre> genres = List.of(new Genre("Genre 1"), new Genre("Genre 2"));

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setDescription("Genre");

        when(genreRepository.findAll()).thenReturn(genres);
        when(modelMapper.map(any(Genre.class), eq(GenreDTO.class))).thenReturn(genreDTO);

        List<GenreDTO> genreDTOs = genreService.findAll();

        assertEquals(genres.size(), genreDTOs.size());
        verify(genreRepository, times(1)).findAll();
        verify(modelMapper, times(genres.size())).map(any(Genre.class), eq(GenreDTO.class));
    }
}