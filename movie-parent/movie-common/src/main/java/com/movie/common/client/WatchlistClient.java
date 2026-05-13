package com.movie.common.client;

import com.movie.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "movie-watchlist")
public interface WatchlistClient {

    @GetMapping("/api/watchlist/movie/{movieId}/watch-count")
    ApiResponse<Integer> getWatchCount(
            @PathVariable Long movieId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
