package com.movie.review.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.review.dto.*;

public interface ReviewService {
    Page<ReviewVO> listReviewsByMovie(Long movieId, long page, long size, Long currentUserId);
    Page<ReviewVO> listReviewsByUser(Long userId, long page, long size, Long currentUserId);
    ReviewVO createReview(Long userId, ReviewCreateRequest request);
    ReviewVO updateReview(Long userId, Long reviewId, ReviewUpdateRequest request);
    void deleteReview(Long userId, Long reviewId, String userRole);
    void likeReview(Long userId, Long reviewId);
    void unlikeReview(Long userId, Long reviewId);
    ReviewStatsVO getMovieStats(Long movieId);
    ReviewStatsVO getMovieStats(Long movieId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}
