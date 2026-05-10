package com.movie.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewVO {
    private Long id;
    private Long movieId;
    private Long userId;
    private String username;
    private String userAvatar;
    private Integer rating;
    private String content;
    private Integer likeCount;
    private Boolean likedByMe;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
