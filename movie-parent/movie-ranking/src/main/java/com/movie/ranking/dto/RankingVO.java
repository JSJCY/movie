package com.movie.ranking.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RankingVO {
    private Integer rank;
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private BigDecimal avgRating;
    private Integer reviewCount;
    private Integer watchCount;
}
