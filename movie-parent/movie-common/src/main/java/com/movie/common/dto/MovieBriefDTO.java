package com.movie.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class MovieBriefDTO {
    private Long id;
    private String title;
    private String posterUrl;
    private BigDecimal averageRating;
}
