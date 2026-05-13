package com.movie.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ReviewStatsDTO {
    private BigDecimal averageRating;
    private Integer ratingCount;
}
