package com.movie.watchlist.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WatchRecordVO {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private String status;
    private Integer rating;
    private LocalDateTime watchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
