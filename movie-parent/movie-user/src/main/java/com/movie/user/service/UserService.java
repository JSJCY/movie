package com.movie.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.user.dto.*;
import com.movie.user.entity.User;

public interface UserService {
    LoginVO login(LoginRequest request);
    LoginVO register(RegisterRequest request);
    LoginVO refreshToken(String refreshToken);
    UserVO getCurrentUser(Long userId);
    UserVO updateUser(Long userId, UpdateUserRequest request);
    UserVO updateAvatar(Long userId, String avatarUrl);
    Page<UserVO> listUsers(long page, long size, String keyword);
    void updateUserStatus(Long userId, Integer status);
    void updateUserRole(Long userId, String role);
    User getById(Long userId);
}
