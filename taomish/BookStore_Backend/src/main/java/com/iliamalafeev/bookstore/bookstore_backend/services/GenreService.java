package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Genre;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.GenreRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {

    private final ModelMapper modelMapper;
    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(ModelMapper modelMapper, GenreRepository genreRepository) {
        this.modelMapper = modelMapper;
        this.genreRepository = genreRepository;
    }

//  <------------------------------------------------------------------------------->
//  <-------------------- Service public methods for controller -------------------->
//  <------------------------------------------------------------------------------->

    public List<GenreDTO> findAll() {

        return genreRepository.findAll().stream().map(this::convertToGenreDTO).collect(Collectors.toList());
    }

//  <-------------------------------------------------------------------------------------------->
//  <-------------------- Service private methods for some code re-usability -------------------->
//  <-------------------------------------------------------------------------------------------->

    private GenreDTO convertToGenreDTO(Genre genre) {
        return modelMapper.map(genre, GenreDTO.class);
    }
}
