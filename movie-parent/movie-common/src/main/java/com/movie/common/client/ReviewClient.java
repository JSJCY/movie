package com.movie.common.client;

import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.ReviewStatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "movie-review")
public interface ReviewClient {

    @GetMapping("/api/reviews/movie/{movieId}/stats")
    ApiResponse<ReviewStatsDTO> getStats(
            @PathVariable Long movieId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
