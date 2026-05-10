package com.movie.movie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.PageResult;
import com.movie.movie.dto.ActorCreateRequest;
import com.movie.movie.entity.Actor;
import com.movie.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/actors")
@RequiredArgsConstructor
public class ActorController {

    private final MovieService movieService;

    @GetMapping
    public ApiResponse<PageResult<Actor>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword) {
        Page<Actor> result = movieService.listActors(page, size, keyword);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    public ApiResponse<Actor> create(@Valid @RequestBody ActorCreateRequest request) {
        return ApiResponse.ok(movieService.createActor(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Actor> update(@PathVariable Long id, @Valid @RequestBody ActorCreateRequest request) {
        return ApiResponse.ok(movieService.updateActor(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        movieService.deleteActor(id);
        return ApiResponse.ok();
    }
}
