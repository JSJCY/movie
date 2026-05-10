package com.movie.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.PageResult;
import com.movie.user.dto.UserVO;
import com.movie.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ApiResponse<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        Page<UserVO> result = userService.listUsers(page, size, keyword);
        return ApiResponse.ok(PageResult.of(
                result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        userService.updateUserStatus(id, body.get("status"));
        return ApiResponse.ok();
    }

    @PutMapping("/users/{id}/role")
    public ApiResponse<Void> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        userService.updateUserRole(id, body.get("role"));
        return ApiResponse.ok();
    }
}
