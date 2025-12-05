package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.GenreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
@WithMockUser
class GenreControllerTest {

    @MockBean private GenreService genreService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    GenreControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void findAll_shouldReturnAllGenres() throws Exception {

        String url = "/api/genres";

        GenreDTO genreDTO1 = new GenreDTO();
        genreDTO1.setDescription("Genre 1");

        GenreDTO genreDTO2 = new GenreDTO();
        genreDTO1.setDescription("Genre 1");

        List<GenreDTO> genreDTOList = List.of(genreDTO1, genreDTO2);

        when(genreService.findAll()).thenReturn(genreDTOList);

        mockMvc.perform(get(url)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(genreDTOList)));

        verify(genreService, times(1)).findAll();
    }
}