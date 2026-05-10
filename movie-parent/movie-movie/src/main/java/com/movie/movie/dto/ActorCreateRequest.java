package com.movie.movie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ActorCreateRequest {

    @NotBlank(message = "演员姓名不能为空")
    private String name;

    private String avatarUrl;
    private String bio;
    private LocalDate birthDate;
    private String nationality;
}
