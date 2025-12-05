package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.DiscussionDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Discussion;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.DiscussionRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.security.entities.Role;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.DiscussionException;
import com.iliamalafeev.bookstore.bookstore_backend.utils.exceptions.PersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscussionServiceTest {

    private final String personEmail = "email@email.com";
    private final String adminEmail = "admin@email.com";
    private final String invalidPersonEmail = "invalidEmail@email.com";
    private final int page = 0;
    private final int discussionsPerPage = 5;

    private Person person;
    private Discussion discussion1;
    private Discussion discussion2;
    private DiscussionDTO discussionDTO;

    @Mock private ModelMapper modelMapper;
    @Mock private DiscussionRepository discussionRepository;
    @Mock private PersonRepository personRepository;

    @InjectMocks
    private DiscussionService discussionService;

    @BeforeEach
    void setUp() {

        person = new Person("First Name", "Last Name", LocalDate.of(1990, 1, 1), personEmail, "Password");
        person.setRole(Role.ROLE_USER);
        person.setRegisteredAt(LocalDateTime.now());
        person.setDiscussions(new ArrayList<>());

        discussion1 = new Discussion(person, "Title 1", "Question 1");
        discussion1.setAdminEmail(null);
        discussion1.setResponse(null);
        discussion1.setClosed(false);

        discussion2 = new Discussion(person, "Title 2", "Question 2");
        discussion2.setAdminEmail(adminEmail);
        discussion2.setResponse("Response");
        discussion2.setClosed(true);

        discussionDTO = new DiscussionDTO();
        discussionDTO.setTitle("title 1");
        discussionDTO.setQuestion("question 1");
        discussionDTO.setAdminEmail(null);
        discussionDTO.setResponse(null);
        discussionDTO.setClosed(false);
    }

    @Test
    void findAllByPersonEmail_shouldReturnAllDiscussionsByPersonEmail() {

        List<Discussion> pageContent = List.of(discussion1, discussion2);
        Pageable pageable = PageRequest.of(page, discussionsPerPage);
        Page<Discussion> discussionsPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(discussionRepository.findByDiscussionHolder(any(Person.class), any(Pageable.class))).thenReturn(discussionsPage);
        when(modelMapper.map(any(Discussion.class), eq(DiscussionDTO.class))).thenReturn(discussionDTO);

        assertNull(discussionDTO.getPersonEmail());
        assertNull(discussionDTO.getPersonFirstName());
        assertNull(discussionDTO.getPersonLastName());

        Page<DiscussionDTO> discussionDTOs = discussionService.findAllByPersonEmail(personEmail, pageable);

        assertNotNull(discussionDTOs);
        assertEquals(pageContent.size(), discussionDTOs.getContent().size());
        assertEquals(personEmail, discussionDTO.getPersonEmail());
        assertEquals(person.getFirstName(), discussionDTO.getPersonFirstName());
        assertEquals(person.getLastName(), discussionDTO.getPersonLastName());
        assertEquals(discussionDTO, discussionDTOs.getContent().get(0));
        assertEquals(discussionDTO, discussionDTOs.getContent().get(1));
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(discussionRepository, times(1)).findByDiscussionHolder(any(Person.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Discussion.class), eq(DiscussionDTO.class));
    }

    @Test
    void findAllByPersonEmail_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        Pageable pageable = PageRequest.of(page, discussionsPerPage);

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> discussionService.findAllByPersonEmail(invalidPersonEmail, pageable));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(discussionRepository, times(0)).findByDiscussionHolder(any(Person.class), any(Pageable.class));
        verify(modelMapper, times(0)).map(any(Discussion.class), eq(DiscussionDTO.class));
    }

    @Test
    void findAllByClosed_shouldReturnAllOpenDiscussions() {

        List<Discussion> pageContent = List.of(discussion1);
        Pageable pageable = PageRequest.of(page, discussionsPerPage);
        Page<Discussion> discussionsPage = new PageImpl<>(pageContent, pageable, pageContent.size());

        when(discussionRepository.findByClosed(any(Boolean.class), any(Pageable.class))).thenReturn(discussionsPage);
        when(modelMapper.map(any(Discussion.class), eq(DiscussionDTO.class))).thenReturn(discussionDTO);

        assertNull(discussionDTO.getPersonEmail());
        assertNull(discussionDTO.getPersonFirstName());
        assertNull(discussionDTO.getPersonLastName());

        Page<DiscussionDTO> discussionDTOs = discussionService.findAllByClosed(pageable);

        assertNotNull(discussionDTOs);
        assertEquals(1, discussionDTOs.getContent().size());
        assertEquals(personEmail, discussionDTO.getPersonEmail());
        assertEquals(person.getFirstName(), discussionDTO.getPersonFirstName());
        assertEquals(person.getLastName(), discussionDTO.getPersonLastName());
        assertEquals(discussionDTO, discussionDTOs.getContent().get(0));
        verify(discussionRepository, times(1)).findByClosed(any(Boolean.class), any(Pageable.class));
        verify(modelMapper, times(pageContent.size())).map(any(Discussion.class), eq(DiscussionDTO.class));
    }

    @Test
    void addDiscussion_shouldAddDiscussionToDatabase() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.of(person));
        when(modelMapper.map(any(DiscussionDTO.class), eq(Discussion.class))).thenReturn(discussion1);
        when(discussionRepository.save(any(Discussion.class))).thenReturn(discussion1);
        when(modelMapper.map(any(Discussion.class), eq(DiscussionDTO.class))).thenReturn(discussionDTO);

        DiscussionDTO savedDiscussionDTO = assertDoesNotThrow(() -> discussionService.addDiscussion(personEmail, discussionDTO, bindingResult));

        assertNotNull(savedDiscussionDTO);
        assertEquals(discussionDTO.getPersonEmail(), savedDiscussionDTO.getPersonEmail());
        assertEquals(discussionDTO.getPersonFirstName(), savedDiscussionDTO.getPersonFirstName());
        assertEquals(discussionDTO.getPersonLastName(), savedDiscussionDTO.getPersonLastName());
        assertEquals(discussionDTO.getTitle(), savedDiscussionDTO.getTitle());
        assertEquals(discussionDTO.getQuestion(), savedDiscussionDTO.getQuestion());
        assertEquals(discussionDTO.getAdminEmail(), savedDiscussionDTO.getAdminEmail());
        assertEquals(discussionDTO.getResponse(), savedDiscussionDTO.getResponse());
        assertEquals(discussionDTO.getClosed(), savedDiscussionDTO.getClosed());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(modelMapper, times(1)).map(any(DiscussionDTO.class), eq(Discussion.class));
        verify(discussionRepository, times(1)).save(any(Discussion.class));
        verify(modelMapper, times(1)).map(any(Discussion.class), eq(DiscussionDTO.class));
    }

    @Test
    void addDiscussion_shouldThrowDiscussionExceptionIfDiscussionDtoIsInvalid() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");
        bindingResult.addError(new FieldError("discussionDTO", "question", "Question must be present and contain at least 1 character"));

        DiscussionException exception = assertThrows(DiscussionException.class, () -> discussionService.addDiscussion(personEmail, discussionDTO, bindingResult));
        assertEquals("Some fields are invalid. question: Question must be present and contain at least 1 character; ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(personRepository, times(0)).findByEmail(any(String.class));
        verify(modelMapper, times(0)).map(any(DiscussionDTO.class), eq(Discussion.class));
        verify(discussionRepository, times(0)).save(any(Discussion.class));
        verify(modelMapper, times(0)).map(any(Discussion.class), eq(DiscussionDTO.class));
    }

    @Test
    void addDiscussion_shouldThrowPersonExceptionIfPersonEmailIsInvalid() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");

        when(personRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());

        PersonException exception = assertThrows(PersonException.class, () -> discussionService.addDiscussion(invalidPersonEmail, discussionDTO, bindingResult));

        assertEquals("Person with such email is not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(personRepository, times(1)).findByEmail(any(String.class));
        verify(modelMapper, times(0)).map(any(DiscussionDTO.class), eq(Discussion.class));
        verify(discussionRepository, times(0)).save(any(Discussion.class));
        verify(modelMapper, times(0)).map(any(Discussion.class), eq(DiscussionDTO.class));
    }

    @Test
    void updateDiscussion_shouldUpdateDiscussionAndSetClosedAndSaveToDataBase() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");

        discussionDTO.setId(1L);
        discussionDTO.setResponse("response");

        when(discussionRepository.findById(any(Long.class))).thenReturn(Optional.of(discussion1));
        when(discussionRepository.save(any(Discussion.class))).thenReturn(discussion1);

        assertNull(discussion1.getAdminEmail());
        assertNull(discussion1.getResponse());
        assertFalse(discussion1.getClosed());

        assertDoesNotThrow(() -> discussionService.updateDiscussion(adminEmail, discussionDTO, bindingResult));

        assertEquals(adminEmail, discussion1.getAdminEmail());
        assertEquals(discussionDTO.getResponse(), discussion1.getResponse());
        assertTrue(discussion1.getClosed());
        verify(discussionRepository, times(1)).findById(any(Long.class));
        verify(discussionRepository, times(1)).save(any(Discussion.class));
    }

    @Test
    void updateDiscussion_shouldThrowDiscussionExceptionIfDiscussionDtoIsInvalid() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");
        bindingResult.addError(new FieldError("discussionDTO", "question", "Question must be present and contain at least 1 character"));

        DiscussionException exception = assertThrows(DiscussionException.class, () -> discussionService.updateDiscussion(adminEmail, discussionDTO, bindingResult));
        assertEquals("Some fields are invalid. question: Question must be present and contain at least 1 character; ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(discussionRepository, times(0)).findById(any(Long.class));
        verify(discussionRepository, times(0)).save(any(Discussion.class));
    }

    @Test
    void updateDiscussion_shouldThrowDiscussionExceptionIfDiscussionDtoDoesNotHaveResponse() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");

        DiscussionException exception = assertThrows(DiscussionException.class, () -> discussionService.updateDiscussion(adminEmail, discussionDTO, bindingResult));
        assertEquals("Discussion cannot be closed without administration response ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(discussionRepository, times(0)).findById(any(Long.class));
        verify(discussionRepository, times(0)).save(any(Discussion.class));
    }

    @Test
    void updateDiscussion_shouldThrowDiscussionExceptionIfDiscussionDoesNotExist() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");

        discussionDTO.setId(1L);
        discussionDTO.setResponse("response");

        when(discussionRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        DiscussionException exception = assertThrows(DiscussionException.class, () -> discussionService.updateDiscussion(adminEmail, discussionDTO, bindingResult));
        assertEquals("Discussion not found. ", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        verify(discussionRepository, times(1)).findById(any(Long.class));
        verify(discussionRepository, times(0)).save(any(Discussion.class));
    }

    @Test
    void updateDiscussion_shouldThrowDiscussionExceptionIfDiscussionIsAlreadyClosed() {

        BindingResult bindingResult = new BindException(discussionDTO, "discussionDTO");

        discussionDTO.setId(1L);
        discussionDTO.setResponse("response");

        when(discussionRepository.findById(any(Long.class))).thenReturn(Optional.of(discussion2));

        DiscussionException exception = assertThrows(DiscussionException.class, () -> discussionService.updateDiscussion(adminEmail, discussionDTO, bindingResult));
        assertEquals("This discussion is already closed. ", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getHttpStatus());
        verify(discussionRepository, times(1)).findById(any(Long.class));
        verify(discussionRepository, times(0)).save(any(Discussion.class));
    }
}