package com.movie.movie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.PageResult;
import com.movie.movie.dto.*;
import com.movie.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ApiResponse<PageResult<MovieVO>> listMovies(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "12") long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "created_at") String sortBy) {
        Page<MovieVO> result = movieService.listMovies(page, size, categoryId, country, keyword, sortBy);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/search")
    public ApiResponse<List<MovieVO>> search(@RequestParam String q) {
        List<MovieVO> movies = movieService.fullTextSearch(q);
        return ApiResponse.ok(movies);
    }

    @GetMapping("/{id}")
    public ApiResponse<MovieVO> getDetail(@PathVariable Long id) {
        MovieVO movie = movieService.getMovieDetail(id);
        return ApiResponse.ok(movie);
    }

    @PostMapping
    public ApiResponse<MovieVO> create(@Valid @RequestBody MovieCreateRequest request) {
        MovieVO movie = movieService.createMovie(request);
        return ApiResponse.ok(movie);
    }

    @PutMapping("/{id}")
    public ApiResponse<MovieVO> update(@PathVariable Long id, @RequestBody MovieUpdateRequest request) {
        MovieVO movie = movieService.updateMovie(id, request);
        return ApiResponse.ok(movie);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ApiResponse.ok();
    }
}
