package com.movie.watchlist.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("watch_record")
public class WatchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long movieId;

    private String status;  // WANT_TO_WATCH / WATCHING / WATCHED

    private Integer rating;

    private LocalDateTime watchedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
