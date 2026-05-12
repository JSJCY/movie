package com.movie.review.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.PageResult;
import com.movie.review.dto.*;
import com.movie.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/movie/{movieId}")
    public ApiResponse<PageResult<ReviewVO>> listByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        Page<ReviewVO> result = reviewService.listReviewsByMovie(movieId, page, size, userId);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<PageResult<ReviewVO>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestHeader(value = "X-User-Id", required = false) Long currentUserId) {
        Page<ReviewVO> result = reviewService.listReviewsByUser(userId, page, size, currentUserId);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    public ApiResponse<ReviewVO> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewVO review = reviewService.createReview(userId, request);
        return ApiResponse.ok(review);
    }

    @PutMapping("/{id}")
    public ApiResponse<ReviewVO> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewVO review = reviewService.updateReview(userId, id, request);
        return ApiResponse.ok(review);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole,
            @PathVariable Long id) {
        reviewService.deleteReview(userId, id, userRole);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Void> like(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        reviewService.likeReview(userId, id);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/like")
    public ApiResponse<Void> unlike(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        reviewService.unlikeReview(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/movie/{movieId}/stats")
    public ApiResponse<ReviewStatsVO> getStats(
            @PathVariable Long movieId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ReviewStatsVO stats = startDate != null && endDate != null
                ? reviewService.getMovieStats(movieId, startDate, endDate)
                : reviewService.getMovieStats(movieId);
        return ApiResponse.ok(stats);
    }
}
