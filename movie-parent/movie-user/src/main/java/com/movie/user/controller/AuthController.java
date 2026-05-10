package com.movie.user.controller;

import com.movie.common.dto.ApiResponse;
import com.movie.user.dto.LoginRequest;
import com.movie.user.dto.LoginVO;
import com.movie.user.dto.RegisterRequest;
import com.movie.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO result = userService.login(request);
        return ApiResponse.ok(result);
    }

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterRequest request) {
        LoginVO result = userService.register(request);
        return ApiResponse.ok(result);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginVO> refresh(@RequestParam String refreshToken) {
        LoginVO result = userService.refreshToken(refreshToken);
        return ApiResponse.ok(result);
    }
}
