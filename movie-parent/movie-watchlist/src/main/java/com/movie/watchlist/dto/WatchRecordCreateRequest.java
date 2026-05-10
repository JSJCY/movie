package com.movie.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WatchRecordCreateRequest {

    @NotNull(message = "电影ID不能为空")
    private Long movieId;

    @NotBlank(message = "状态不能为空")
    private String status;  // WANT_TO_WATCH / WATCHING / WATCHED

    private Integer rating;
}
