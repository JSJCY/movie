package com.movie.movie.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("movie")
public class Movie {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String originalTitle;

    private LocalDate releaseDate;

    private Integer duration;

    private String country;

    private String language;

    private String description;

    private String posterUrl;

    private String trailerUrl;

    private BigDecimal averageRating;

    private Integer ratingCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
