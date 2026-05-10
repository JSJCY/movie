package com.movie.review.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ReviewStatsVO {
    private BigDecimal averageRating;
    private Integer ratingCount;
    private Long reviewCount;
}
