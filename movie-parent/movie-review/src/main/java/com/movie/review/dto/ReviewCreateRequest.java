package com.movie.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewCreateRequest {

    @NotNull(message = "电影ID不能为空")
    private Long movieId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 10, message = "评分最大为10")
    private Integer rating;

    private String content;
}
