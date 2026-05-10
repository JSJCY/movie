package com.movie.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private UserVO user;
}
