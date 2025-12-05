package com.iliamalafeev.bookstore.bookstore_backend.services;

import com.iliamalafeev.bookstore.bookstore_backend.dto.BookDTO;
import com.iliamalafeev.bookstore.bookstore_backend.dto.HistoryRecordDTO;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Book;
import com.iliamalafeev.bookstore.bookstore_backend.entities.HistoryRecord;
import com.iliamalafeev.bookstore.bookstore_backend.entities.Person;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.HistoryRecordRepository;
import com.iliamalafeev.bookstore.bookstore_backend.repositories.PersonRepository;
import com.iliamalafeev.bookstore.bookstore_backend.utils.ErrorsUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HistoryRecordService {

    private final ModelMapper modelMapper;
    private final HistoryRecordRepository historyRecordRepository;
    private final PersonRepository personRepository;

    @Autowired
    public HistoryRecordService(ModelMapper modelMapper, HistoryRecordRepository historyRecordRepository, PersonRepository personRepository) {
        this.modelMapper = modelMapper;
        this.historyRecordRepository = historyRecordRepository;
        this.personRepository = personRepository;
    }

//  <------------------------------------------------------------------------------->
//  <-------------------- Service public methods for controller -------------------->
//  <------------------------------------------------------------------------------->

    public Page<HistoryRecordDTO> findAllByPersonEmail(String personEmail, Pageable pageable) {

        Person person = getPersonFromRepository(personEmail);

        Page<HistoryRecord> historyRecords = historyRecordRepository.findByHistoryRecordHolder(person, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()));

        List<HistoryRecordDTO> pageContent = new ArrayList<>();

        for (HistoryRecord historyRecord : historyRecords) {
            HistoryRecordDTO historyRecordDTO = convertToHistoryRecordDTO(historyRecord);
            historyRecordDTO.setBookDTO(convertToBookDTO(historyRecord.getHistoryRecordedBook()));
            pageContent.add(historyRecordDTO);
        }

        return new PageImpl<>(pageContent, historyRecords.getPageable(), historyRecords.getTotalElements());
    }

//  <-------------------------------------------------------------------------------------------->
//  <-------------------- Service private methods for some code re-usability -------------------->
//  <-------------------------------------------------------------------------------------------->

    private Person getPersonFromRepository(String personEmail) {

        Optional<Person> person = personRepository.findByEmail(personEmail);

        if (person.isEmpty()) {
            ErrorsUtil.returnPersonError("Person with such email is not found.", null, HttpStatus.NOT_FOUND);
        }

        return person.get();
    }

    private HistoryRecordDTO convertToHistoryRecordDTO(HistoryRecord historyRecord) {
        return modelMapper.map(historyRecord, HistoryRecordDTO.class);
    }

    private BookDTO convertToBookDTO(Book book) {
        return modelMapper.map(book, BookDTO.class);
    }
}
