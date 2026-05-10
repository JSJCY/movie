package com.movie.user.controller;

import com.movie.common.dto.ApiResponse;
import com.movie.user.dto.UpdateUserRequest;
import com.movie.user.dto.UserVO;
import com.movie.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        UserVO user = userService.getCurrentUser(userId);
        return ApiResponse.ok(user);
    }

    @PutMapping("/me")
    public ApiResponse<UserVO> updateUser(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserVO user = userService.updateUser(userId, request);
        return ApiResponse.ok(user);
    }

    @PutMapping("/me/avatar")
    public ApiResponse<UserVO> updateAvatar(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam String avatarUrl) {
        UserVO user = userService.updateAvatar(userId, avatarUrl);
        return ApiResponse.ok(user);
    }
}
