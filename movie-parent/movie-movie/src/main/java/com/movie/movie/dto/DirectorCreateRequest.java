package com.movie.movie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DirectorCreateRequest {

    @NotBlank(message = "导演姓名不能为空")
    private String name;

    private String avatarUrl;
    private String bio;
    private LocalDate birthDate;
    private String nationality;
}
