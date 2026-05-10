package com.movie.movie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.PageResult;
import com.movie.movie.dto.DirectorCreateRequest;
import com.movie.movie.entity.Director;
import com.movie.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final MovieService movieService;

    @GetMapping
    public ApiResponse<PageResult<Director>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        Page<Director> result = movieService.listDirectors(page, size, keyword);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    public ApiResponse<Director> create(@Valid @RequestBody DirectorCreateRequest request) {
        return ApiResponse.ok(movieService.createDirector(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Director> update(@PathVariable Long id, @Valid @RequestBody DirectorCreateRequest request) {
        return ApiResponse.ok(movieService.updateDirector(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        movieService.deleteDirector(id);
        return ApiResponse.ok();
    }
}
