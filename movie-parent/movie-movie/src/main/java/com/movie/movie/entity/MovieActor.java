package com.movie.movie.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("movie_actor")
public class MovieActor {
    private Long movieId;
    private Long actorId;
    private String characterName;
}
