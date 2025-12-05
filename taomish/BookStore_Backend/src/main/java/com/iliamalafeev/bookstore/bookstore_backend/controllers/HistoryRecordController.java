package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.iliamalafeev.bookstore.bookstore_backend.dto.HistoryRecordDTO;
import com.iliamalafeev.bookstore.bookstore_backend.security.jwt.JwtUtils;
import com.iliamalafeev.bookstore.bookstore_backend.services.HistoryRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:5173/")
@RestController
@RequestMapping("/api/history-records/secure")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "History Record Controller")
public class HistoryRecordController {

    private final HistoryRecordService historyRecordService;
    private final JwtUtils jwtUtils;

    @Autowired
    public HistoryRecordController(HistoryRecordService historyRecordService, JwtUtils jwtUtils) {
        this.historyRecordService = historyRecordService;
        this.jwtUtils = jwtUtils;
    }

    private String extractEmail(String token) {
        String jwt = token.substring(7);
        return jwtUtils.extractPersonEmail(jwt);
    }

    @Operation(summary = "Get a paginated list of history records for an authenticated user.",
            description = "Returns a page containing HistoryRecordDTO objects.")
    @GetMapping
    public ResponseEntity<Page<HistoryRecordDTO>> findAllByPersonEmail(@RequestHeader("Authorization") String token,
                                                                       @RequestParam(value = "page") Integer page,
                                                                       @RequestParam(value = "records-per-page") Integer recordsPerPage) {

        Page<HistoryRecordDTO> responseBody = historyRecordService.findAllByPersonEmail(extractEmail(token), PageRequest.of(page, recordsPerPage));
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
