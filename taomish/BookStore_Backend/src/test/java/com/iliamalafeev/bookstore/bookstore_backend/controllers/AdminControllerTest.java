package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.DiscussionDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.GenreDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.security.services.PersonDetailsService;
import com.iliamalafeev.bookstore.bookstore_backend.services.BookService;
import com.iliamalafeev.bookstore.bookstore_backend.services.DiscussionService;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.BookException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.DiscussionException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.GenreException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser
class AdminControllerTest {

    private final Long bookId = 1L;
    private final Long invalidBookId = 1000L;
    private final String jwtToken = "TestJWT";
    private final String adminEmail = "adminEmail@email.com";
    private final String baseURL = "/api/admin/secure";

    private BookDTO bookDTO;
    private BookDTO savedBookDTO;
    private DiscussionDTO discussionDTO1;
    private DiscussionDTO discussionDTO2;
    private DiscussionDTO respondedDiscussionDTO1;

    @MockBean private BookService bookService;
    @MockBean private DiscussionService discussionService;
    @MockBean private JwtUtils jwtUtils;
    @MockBean private PersonDetailsService personDetailsService;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @Autowired
    AdminControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {

        String personEmail = "email@email.com";

        GenreDTO genreDTO = new GenreDTO();
        genreDTO.setDescription("Genre 1");

        bookDTO = new BookDTO();
        bookDTO.setTitle("Title 1");
        bookDTO.setAuthor("Author 1");
        bookDTO.setDescription("Description 1");
        bookDTO.setCopies(10);
        bookDTO.setCopiesAvailable(10);
        bookDTO.setImg("encodedImage 1");
        bookDTO.setGenres(List.of(genreDTO));

        savedBookDTO = new BookDTO();
        savedBookDTO.setId(1L);
        savedBookDTO.setTitle("Title 1");
        savedBookDTO.setAuthor("Author 1");
        savedBookDTO.setDescription("Description 1");
        savedBookDTO.setCopies(10);
        savedBookDTO.setCopiesAvailable(10);
        savedBookDTO.setImg("encodedImage 1");
        savedBookDTO.setGenres(List.of(genreDTO));

        discussionDTO1 = new DiscussionDTO();
        discussionDTO1.setId(1L);
        discussionDTO1.setPersonEmail(personEmail);
        discussionDTO1.setPersonFirstName("First Name 1");
        discussionDTO1.setPersonLastName("Last Name 1");
        discussionDTO1.setTitle("Title 1");
        discussionDTO1.setQuestion("Question 1");
        discussionDTO1.setAdminEmail(null);
        discussionDTO1.setResponse(null);
        discussionDTO1.setClosed(false);

        discussionDTO2 = new DiscussionDTO();
        discussionDTO2.setId(2L);
        discussionDTO2.setPersonEmail(personEmail);
        discussionDTO2.setPersonFirstName("First Name 2");
        discussionDTO2.setPersonLastName("Last Name 2");
        discussionDTO2.setTitle("Title 2");
        discussionDTO2.setQuestion("Question 2");
        discussionDTO2.setAdminEmail(null);
        discussionDTO2.setResponse(null);
        discussionDTO2.setClosed(false);

        respondedDiscussionDTO1 = new DiscussionDTO();
        respondedDiscussionDTO1.setId(1L);
        respondedDiscussionDTO1.setPersonEmail(personEmail);
        respondedDiscussionDTO1.setPersonFirstName("First Name 1");
        respondedDiscussionDTO1.setPersonLastName("Last Name 1");
        respondedDiscussionDTO1.setTitle("Title 1");
        respondedDiscussionDTO1.setQuestion("Question 1");
        respondedDiscussionDTO1.setAdminEmail(null);
        respondedDiscussionDTO1.setResponse("Response 1");
        respondedDiscussionDTO1.setClosed(false);
    }

    @Test
    void postBook_shouldAddNewBookAndReturnSavedBookDTO() throws Exception {

        String url = baseURL + "/add-book";
        String dtoJson = objectMapper.writeValueAsString(bookDTO);

        when(bookService.addBook(any(BookDTO.class), any(BindingResult.class))).thenReturn(savedBookDTO);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(savedBookDTO)));

        verify(bookService, times(1)).addBook(any(BookDTO.class), any(BindingResult.class));
    }

    @Test
    void postBook_shouldReturnForbiddenIfBookDtoIsInvalid() throws Exception {

        String url = baseURL + "/add-book";
        String dtoJson = objectMapper.writeValueAsString(bookDTO);
        BookException exception = new BookException("Some fields are invalid. title: Book with this title from this author already exists; ", HttpStatus.FORBIDDEN);

        when(bookService.addBook(any(BookDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Some fields are invalid. title: Book with this title from this author already exists; "));

        verify(bookService, times(1)).addBook(any(BookDTO.class), any(BindingResult.class));
    }

    @Test
    void postBook_shouldThrowGenreExceptionIfGenresAreInvalid() throws Exception {

        String url = baseURL + "/add-book";
        String dtoJson = objectMapper.writeValueAsString(bookDTO);
        GenreException exception = new GenreException("No such genres found ", HttpStatus.NOT_FOUND);

        when(bookService.addBook(any(BookDTO.class), any(BindingResult.class))).thenThrow(exception);

        mockMvc.perform(post(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No such genres found "));

        verify(bookService, times(1)).addBook(any(BookDTO.class), any(BindingResult.class));
    }

    @Test
    void increaseBookQuantity_shouldIncreaseBookQuantity() throws Exception {

        String url = baseURL + "/increase-quantity/{bookId}";

        doNothing().when(bookService).changeQuantity(any(Long.class), any(String.class));

        mockMvc.perform(patch(url, bookId)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).changeQuantity(any(Long.class), any(String.class));
    }

    @Test
    void increaseBookQuantity_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/increase-quantity/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        doThrow(exception).when(bookService).changeQuantity(any(Long.class), any(String.class));

        mockMvc.perform(patch(url, invalidBookId)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(bookService, times(1)).changeQuantity(any(Long.class), any(String.class));
    }

    @Test
    void decreaseBookQuantity_shouldDecreaseBookQuantity() throws Exception {

        String url = baseURL + "/decrease-quantity/{bookId}";

        doNothing().when(bookService).changeQuantity(any(Long.class), any(String.class));

        mockMvc.perform(patch(url, bookId)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).changeQuantity(any(Long.class), any(String.class));
    }

    @Test
    void decreaseBookQuantity_shouldReturnNotFoundIfBookIdIsInvalid() throws Exception {

        String url = baseURL + "/decrease-quantity/{bookId}";
        BookException exception = new BookException("Book not found ", HttpStatus.NOT_FOUND);

        doThrow(exception).when(bookService).changeQuantity(any(Long.class), any(String.class));

        mockMvc.perform(patch(url, invalidBookId)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found "));

        verify(bookService, times(1)).changeQuantity(any(Long.class), any(String.class));
    }

    @Test
    void decreaseBookQuantity_shouldReturnForbiddenIfCopiesOrCopiesAvailableIsAlreadyZero() throws Exception {

        String url = baseURL + "/decrease-quantity/{bookId}";
        BookException exception = new BookException("Book quantity is already 0 ", HttpStatus.FORBIDDEN);

        doThrow(exception).when(bookService).changeQuantity(any(Long.class), any(String.class));

        mockMvc.perform(patch(url, bookId)
                        .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Book quantity is already 0 "));

        verify(bookService, times(1)).changeQuantity(any(Long.class), any(String.class));
    }

    @Test
    void deleteBook_shouldDeleteBookById() throws Exception {

        String url = baseURL + "/delete-book/{bookId}";

        doNothing().when(bookService).deleteById(any(Long.class));

         mockMvc.perform(delete(url, bookId)
                         .with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                 .andExpect(status().isNoContent());

         verify(bookService, times(1)).deleteById(any(Long.class));
    }

    @Test
    void findAllUnclosedDiscussions_shouldReturnAllOpenDiscussions() throws Exception {

        String url = baseURL + "/open-discussions";
        int page = 0;
        int discussionsPerPage = 5;

        List<DiscussionDTO> pageContent = List.of(discussionDTO1, discussionDTO2);
        Pageable pageable = PageRequest.of(page, discussionsPerPage);
        Page<DiscussionDTO> discussionDTOPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(discussionService.findAllByClosed(any(Pageable.class))).thenReturn(discussionDTOPage);

        mockMvc.perform(get(url)
                        .param("page", String.valueOf(page))
                        .param("discussions-per-page", String.valueOf(discussionsPerPage))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(discussionDTOPage)));

        verify(discussionService, times(1)).findAllByClosed(any(Pageable.class));
    }

    @Test
    void updateDiscussion_shouldUpdateDiscussionAndSetClosed() throws Exception {

        String url = baseURL + "/close-discussion";
        String dtoJson = objectMapper.writeValueAsString(respondedDiscussionDTO1);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(adminEmail);
        doNothing().when(discussionService).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));

        mockMvc.perform(patch(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfDiscussionDtoIsInvalid() throws Exception {

        String url = baseURL + "/close-discussion";
        String dtoJson = objectMapper.writeValueAsString(respondedDiscussionDTO1);
        DiscussionException exception = new DiscussionException("Some fields are invalid. question: Question must be present and contain at least 1 character; ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(adminEmail);
        doThrow(exception).when(discussionService).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));

        mockMvc.perform(patch(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Some fields are invalid. question: Question must be present and contain at least 1 character; "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfDiscussionDtoDoesNotHaveResponse() throws Exception {

        String url = baseURL + "/close-discussion";
        String dtoJson = objectMapper.writeValueAsString(respondedDiscussionDTO1);
        DiscussionException exception = new DiscussionException("Discussion cannot be closed without administration response ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(adminEmail);
        doThrow(exception).when(discussionService).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));

        mockMvc.perform(patch(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Discussion cannot be closed without administration response "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }

    @Test
    void updateDiscussion_shouldReturnNotFoundIfDiscussionDoesNotExist() throws Exception {

        String url = baseURL + "/close-discussion";
        String dtoJson = objectMapper.writeValueAsString(respondedDiscussionDTO1);
        DiscussionException exception = new DiscussionException("Discussion not found. ", HttpStatus.NOT_FOUND);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(adminEmail);
        doThrow(exception).when(discussionService).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));

        mockMvc.perform(patch(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Discussion not found. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }

    @Test
    void updateDiscussion_shouldReturnForbiddenIfDiscussionIsAlreadyClosed() throws Exception {

        String url = baseURL + "/close-discussion";
        String dtoJson = objectMapper.writeValueAsString(respondedDiscussionDTO1);
        DiscussionException exception = new DiscussionException("This discussion is already closed. ", HttpStatus.FORBIDDEN);

        when(jwtUtils.extractPersonEmail(any(String.class))).thenReturn(adminEmail);
        doThrow(exception).when(discussionService).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));

        mockMvc.perform(patch(url)
                        .with(csrf())
                        .content(dtoJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This discussion is already closed. "));

        verify(jwtUtils, times(2)).extractPersonEmail(any(String.class));
        verify(discussionService, times(1)).updateDiscussion(any(String.class), any(DiscussionDTO.class), any(BindingResult.class));
    }
}