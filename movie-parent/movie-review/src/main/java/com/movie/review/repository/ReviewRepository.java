package com.movie.review.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewRepository extends BaseMapper<Review> {

    @Select("SELECT COALESCE(AVG(rating), 0) FROM review WHERE movie_id = #{movieId}")
    Double getAverageRating(Long movieId);

    @Select("SELECT COUNT(*) FROM review WHERE movie_id = #{movieId}")
    Integer getRatingCount(Long movieId);
}
