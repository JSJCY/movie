package com.movie.movie.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MovieBriefVO {

    private Long id;
    private String title;
    private String posterUrl;
    private BigDecimal averageRating;
}
