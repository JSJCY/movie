package com.movie.ranking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ranking_snapshot")
public class RankingSnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String periodType;    // WEEKLY / MONTHLY

    private LocalDate periodStart;

    private LocalDate periodEnd;

    private Long movieId;

    private Integer rank;

    private BigDecimal avgRating;

    private Integer reviewCount;

    private Integer watchCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
