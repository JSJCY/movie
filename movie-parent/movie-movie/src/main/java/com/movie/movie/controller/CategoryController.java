package com.movie.movie.controller;

import com.movie.common.dto.ApiResponse;
import com.movie.movie.dto.CategoryCreateRequest;
import com.movie.movie.entity.Category;
import com.movie.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final MovieService movieService;

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.ok(movieService.listCategories());
    }

    @PostMapping
    public ApiResponse<Category> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ApiResponse.ok(movieService.createCategory(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        movieService.deleteCategory(id);
        return ApiResponse.ok();
    }
}
