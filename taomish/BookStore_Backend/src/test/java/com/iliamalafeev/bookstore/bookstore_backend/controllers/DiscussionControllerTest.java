package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.DiscussionDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.DiscussionService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.DiscussionException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DiscussionController.class)
@WithMockUser
class DiscussionControllerTest {

    private final String jwtToken = "TestJWT";
    private final String personEmail = "email@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";
    private final int page = 0;
    private final int discussionsPerPage = 5;
    private final String baseURL = "/api/discussions/secure";

    private DiscussionDTO discussionDTO1;
    private DiscussionDTO discussionDTO2;
    private DiscussionDTO savedDiscussionDTO;

    @MockBean private DiscussionService discussionService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    DiscussionControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        discussionDTO1 = new DiscussionDTO();
        discussionDTO1.setTitle("Title 1");
        discussionDTO1.setQuestion("Question 1");
        discussionDTO1.setAdminEmail(null);
        discussionDTO1.setResponse(null);
        discussionDTO1.setClosed(false);

        discussionDTO2 = new DiscussionDTO();
        discussionDTO2.setTitle("Title 2");
        discussionDTO2.setQuestion("Question 2");
        discussionDTO2.setAdminEmail(null);
        discussionDTO2.setResponse(null);
        discussionDTO2.setClosed(false);

        savedDiscussionDTO = new DiscussionDTO();
        savedDiscussionDTO.setId(1L);
        savedDiscussionDTO.setPersonEmail(personEmail);
        savedDiscussionDTO.setPersonFirstName("First Name 1");
        savedDiscussionDTO.setPersonLastName("Last Name 1");
        savedDiscussionDTO.setTitle("Title 1");
        savedDiscussionDTO.setQuestion("Question 1");
        savedDiscussionDTO.setAdminEmail(null);
        savedDiscussionDTO.setResponse(null);
        savedDiscussionDTO.setClosed(false);
    }

    @Test
    void findAllByPersonEmail_shouldReturnAllDiscussionsByPersonEmail() throws Exception {

        List<DiscussionDTO> pageContent = List.of(discussionDTO1, discussionDTO2);
        Pageable pageable = PageRequest.of(page, discussionsPerPage);
        Page<DiscussionDTO> discussionDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(discussionService.findAllByPersonEmail(any(String.class), any(Pageable.class))).thenReturn(discussionDTOPage);

        mockMvc.perform(get(baseURL)
                        .param("page", String.valueOf(page))
                        .param("discussions-per-page", String.valueOf(discussionsPerPage))
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(discussionDTOPage)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).findAllByPersonEmail(any(String.class), any(Pageable.class));
    }

    @Test
    void findAllByPersonEmail_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(discussionService.findAllByPersonEmail(any(String.class), any(Pageable.class))).thenThrow(exception);

        mockMvc.perform(get(baseURL)
                        .param("page", String.valueOf(page))
                        .param("discussions-per-page", String.valueOf(discussionsPerPage))
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).findAllByPersonEmail(any(String.class), any(Pageable.class));
    }

    @Test
    void addDiscussion_shouldAddDiscussionToDatabaseAndReturnIt() throws Exception {

        String url = baseURL + "/add-discussion";
        String dtoJson = objectMapper.writeValueAsString(discussionDTO1);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(discussionService.addDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class))).thenReturn(savedDiscussionDTO);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(savedDiscussionDTO)));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).addDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }

    @Test
    void addDiscussion_shouldReturnForbiddenIfDiscussionDtoIsInvalid() throws Exception {

        String url = baseURL + "/add-discussion";
        String dtoJson = objectMapper.writeValueAsString(discussionDTO1);
        DiscussionException exception = new DiscussionException("Some fields are invalid. question: Question must be present and contain at least 1 character; ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(personEmail);
        when(discussionService.addDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Some fields are invalid. question: Question must be present and contain at least 1 character; "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).addDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }

    @Test
    void addDiscussion_shouldReturnNotFoundIfPersonEmailIsInvalid() throws Exception {

        String url = baseURL + "/add-discussion";
        String dtoJson = objectMapper.writeValueAsString(discussionDTO1);
        PersonException exception = new PersonException("Person with such email is not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(invalidPersonEmail);
        when(discussionService.addDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Person with such email is not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).addDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }
}