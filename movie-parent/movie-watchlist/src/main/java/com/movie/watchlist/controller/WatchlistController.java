package com.movie.watchlist.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.PageResult;
import com.movie.watchlist.dto.*;
import com.movie.watchlist.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @GetMapping("/me")
    public ApiResponse<PageResult<WatchRecordVO>> listMy(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        Page<WatchRecordVO> result = watchlistService.listMyWatchlist(userId, status, page, size);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @GetMapping("/me/history")
    public ApiResponse<PageResult<WatchRecordVO>> listHistory(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size) {
        Page<WatchRecordVO> result = watchlistService.listMyHistory(userId, page, size);
        return ApiResponse.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PostMapping
    public ApiResponse<WatchRecordVO> add(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody WatchRecordCreateRequest request) {
        WatchRecordVO record = watchlistService.addRecord(userId, request);
        return ApiResponse.ok(record);
    }

    @PutMapping("/{id}")
    public ApiResponse<WatchRecordVO> update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody WatchRecordUpdateRequest request) {
        WatchRecordVO record = watchlistService.updateRecord(userId, id, request);
        return ApiResponse.ok(record);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        watchlistService.deleteRecord(userId, id);
        return ApiResponse.ok();
    }

    @GetMapping("/movie/{movieId}/my-status")
    public ApiResponse<WatchRecordVO> getMyStatus(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long movieId) {
        WatchRecordVO status = watchlistService.getMyStatus(userId, movieId);
        return ApiResponse.ok(status);
    }
}
