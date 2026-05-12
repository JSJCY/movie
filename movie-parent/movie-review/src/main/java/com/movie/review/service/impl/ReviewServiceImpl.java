package com.movie.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.exception.BusinessException;
import com.movie.review.dto.*;
import com.movie.review.entity.Review;
import com.movie.review.entity.ReviewLike;
import com.movie.review.repository.ReviewLikeRepository;
import com.movie.review.repository.ReviewRepository;
import com.movie.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    @Override
    public Page<ReviewVO> listReviewsByMovie(Long movieId, long page, long size, Long currentUserId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getMovieId, movieId)
               .orderByDesc(Review::getCreatedAt);

        Page<Review> reviewPage = reviewRepository.selectPage(new Page<>(page, size), wrapper);

        Page<ReviewVO> voPage = new Page<>(page, size);
        voPage.setTotal(reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream()
                .map(r -> toReviewVO(r, currentUserId))
                .toList());
        return voPage;
    }

    @Override
    public Page<ReviewVO> listReviewsByUser(Long userId, long page, long size, Long currentUserId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
               .orderByDesc(Review::getCreatedAt);

        Page<Review> reviewPage = reviewRepository.selectPage(new Page<>(page, size), wrapper);

        Page<ReviewVO> voPage = new Page<>(page, size);
        voPage.setTotal(reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream()
                .map(r -> toReviewVO(r, currentUserId))
                .toList());
        return voPage;
    }

    @Override
    @Transactional
    public ReviewVO createReview(Long userId, ReviewCreateRequest request) {
        // 检查是否已评价过该电影
        if (reviewRepository.exists(new LambdaQueryWrapper<Review>()
                .eq(Review::getMovieId, request.getMovieId())
                .eq(Review::getUserId, userId))) {
            throw new BusinessException(400, "您已评价过这部电影，可以修改之前的评价");
        }

        Review review = new Review();
        review.setMovieId(request.getMovieId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setLikeCount(0);
        reviewRepository.insert(review);

        return toReviewVO(review, userId);
    }

    @Override
    @Transactional
    public ReviewVO updateReview(Long userId, Long reviewId, ReviewUpdateRequest request) {
        Review review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "影评不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(403, "只能修改自己的影评");
        }

        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getContent() != null) review.setContent(request.getContent());
        reviewRepository.updateById(review);

        return toReviewVO(review, userId);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId, String userRole) {
        Review review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "影评不存在");
        }
        if (!review.getUserId().equals(userId) && !"ADMIN".equals(userRole)) {
            throw new BusinessException(403, "无权删除此影评");
        }
        reviewRepository.deleteById(reviewId);
        // 同时删除关联的点赞
        reviewLikeRepository.delete(new LambdaQueryWrapper<ReviewLike>().eq(ReviewLike::getReviewId, reviewId));
    }

    @Override
    @Transactional
    public void likeReview(Long userId, Long reviewId) {
        Review review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "影评不存在");
        }

        if (reviewLikeRepository.exists(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getReviewId, reviewId)
                .eq(ReviewLike::getUserId, userId))) {
            throw new BusinessException(400, "已点赞过");
        }

        ReviewLike like = new ReviewLike();
        like.setReviewId(reviewId);
        like.setUserId(userId);
        reviewLikeRepository.insert(like);

        // 更新点赞计数
        review.setLikeCount(review.getLikeCount() + 1);
        reviewRepository.updateById(review);
    }

    @Override
    @Transactional
    public void unlikeReview(Long userId, Long reviewId) {
        Review review = reviewRepository.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "影评不存在");
        }

        reviewLikeRepository.delete(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getReviewId, reviewId)
                .eq(ReviewLike::getUserId, userId));

        review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
        reviewRepository.updateById(review);
    }

    @Override
    public ReviewStatsVO getMovieStats(Long movieId) {
        Double avg = reviewRepository.getAverageRating(movieId);
        Integer ratingCount = reviewRepository.getRatingCount(movieId);
        Long reviewCount = getContentReviewCount(movieId, null, null);

        return ReviewStatsVO.builder()
                .averageRating(avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .ratingCount(ratingCount)
                .reviewCount(reviewCount)
                .build();
    }

    @Override
    public ReviewStatsVO getMovieStats(Long movieId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        Double avg = reviewRepository.getAverageRatingByDate(movieId, startDate, endDate);
        Integer ratingCount = reviewRepository.getRatingCountByDate(movieId, startDate, endDate);
        Long reviewCount = getContentReviewCount(movieId, startDate, endDate);

        return ReviewStatsVO.builder()
                .averageRating(avg != null ? BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .ratingCount(ratingCount)
                .reviewCount(reviewCount)
                .build();
    }

    private Long getContentReviewCount(Long movieId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getMovieId, movieId)
                .isNotNull(Review::getContent)
                .ne(Review::getContent, "");
        if (startDate != null && endDate != null) {
            wrapper.ge(Review::getCreatedAt, startDate)
                   .lt(Review::getCreatedAt, endDate);
        }
        return reviewRepository.selectCount(wrapper);
    }

    private ReviewVO toReviewVO(Review review, Long currentUserId) {
        boolean likedByMe = currentUserId != null && reviewLikeRepository.exists(
                new LambdaQueryWrapper<ReviewLike>()
                        .eq(ReviewLike::getReviewId, review.getId())
                        .eq(ReviewLike::getUserId, currentUserId));

        return ReviewVO.builder()
                .id(review.getId())
                .movieId(review.getMovieId())
                .userId(review.getUserId())
                .rating(review.getRating())
                .content(review.getContent())
                .likeCount(review.getLikeCount())
                .likedByMe(likedByMe)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
