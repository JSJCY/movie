package com.movie.watchlist.dto;

import lombok.Data;

@Data
public class WatchRecordUpdateRequest {
    private String status;
    private Integer rating;
}
