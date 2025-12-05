package com.iliamalafeev.bookstore.bookstore_backend.controllers;

import com.iliamalafeev.bookstore.bookstore_backend.dto.ReviewDTO;
import com.iliamalafeev.bookstore.bookstore_backend.services.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("http://localhost:5173/")
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Review Controller")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get a paginated list of reviews for a specific book.",
            description = "Returns a page containing ReviewDTO objects.")
    @GetMapping("/{bookId}")
    public ResponseEntity<Page<ReviewDTO>> findAllByBookId(@PathVariable("bookId") Long bookId,
                                                           @RequestParam(value = "page") Integer page,
                                                           @RequestParam(value = "reviews-per-page") Integer reviewsPerPage,
                                                           @RequestParam(value = "latest", defaultValue = "false") boolean latest) {

        Page<ReviewDTO> responseBody = reviewService.findAllByBookId(bookId, PageRequest.of(page, reviewsPerPage), latest);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @Operation(summary = "Get an average rating for a specific book.",
            description = "Counts an average rating across all the reviews related to selected book. Returns a value of type Double.")
    @GetMapping("/average-rating/{bookId}")
    public ResponseEntity<Double> getAverageRatingByBookId(@PathVariable("bookId") Long bookId) {

        Double responseBody = reviewService.getAverageRatingByBookId(bookId);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }
}
