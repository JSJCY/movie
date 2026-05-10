package com.movie.review.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_like")
public class ReviewLike {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long reviewId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
