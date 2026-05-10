package com.movie.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("director")
public class Director {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String avatarUrl;

    private String bio;

    private LocalDate birthDate;

    private String nationality;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
