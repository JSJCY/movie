package com.movie.movie.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("movie_category")
public class MovieCategory {
    private Long movieId;
    private Long categoryId;
}
