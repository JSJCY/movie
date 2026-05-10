package com.movie.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.exception.BusinessException;
import com.movie.common.util.JwtUtil;
import com.movie.user.dto.*;
import com.movie.user.entity.User;
import com.movie.user.repository.UserRepository;
import com.movie.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userRepository.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public LoginVO register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()))) {
            throw new BusinessException(400, "用户名已存在");
        }
        // 检查邮箱是否被占用
        if (request.getEmail() != null && userRepository.exists(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()))) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("USER");
        user.setStatus(1);
        userRepository.insert(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        try {
            Claims claims = JwtUtil.parseToken(refreshToken);
            Long userId = JwtUtil.getUserId(claims);
            User user = userRepository.selectById(userId);
            if (user == null || user.getStatus() == 0) {
                throw new BusinessException(401, "用户不存在或已禁用");
            }
            return buildLoginVO(user);
        } catch (Exception e) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = getById(userId);
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long userId, UpdateUserRequest request) {
        User user = getById(userId);

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.exists(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail()))) {
                throw new BusinessException(400, "邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        userRepository.updateById(user);
        return toUserVO(user);
    }

    @Override
    public UserVO updateAvatar(Long userId, String avatarUrl) {
        User user = getById(userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.updateById(user);
        return toUserVO(user);
    }

    @Override
    public Page<UserVO> listUsers(long page, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword)
                   .or().like(User::getEmail, keyword)
                   .or().like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> userPage = new Page<>(page, size);
        Page<User> result = userRepository.selectPage(userPage, wrapper);

        Page<UserVO> voPage = new Page<>(page, size);
        voPage.setTotal(result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toUserVO).toList());
        return voPage;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        User user = getById(userId);
        user.setStatus(status);
        userRepository.updateById(user);
    }

    @Override
    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = getById(userId);
        user.setRole(role);
        userRepository.updateById(user);
    }

    @Override
    public User getById(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private LoginVO buildLoginVO(User user) {
        String accessToken = JwtUtil.createAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = JwtUtil.createRefreshToken(user.getId());
        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(toUserVO(user))
                .build();
    }

    private UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
