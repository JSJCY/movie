package com.movie.review.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.review.entity.ReviewLike;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewLikeRepository extends BaseMapper<ReviewLike> {
}
