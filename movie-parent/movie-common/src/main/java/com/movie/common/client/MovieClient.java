package com.movie.common.client;

import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.MovieBriefDTO;
import com.movie.common.dto.MovieSummaryDTO;
import com.movie.common.dto.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "movie-movie")
public interface MovieClient {

    @GetMapping("/api/movies")
    ApiResponse<PageResult<MovieSummaryDTO>> listMovies(
            @RequestParam("page") long page,
            @RequestParam("size") long size);

    @GetMapping("/api/movies/batch")
    ApiResponse<List<MovieBriefDTO>> getMovieBriefs(@RequestParam("ids") List<Long> ids);
}
