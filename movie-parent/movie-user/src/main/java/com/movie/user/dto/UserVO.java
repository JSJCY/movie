package com.movie.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
}
