package com.movie.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReviewUpdateRequest {
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 10, message = "评分最大为10")
    private Integer rating;

    private String content;
}
