package com.movie.review.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface ReviewRepository extends BaseMapper<Review> {

    @Select("SELECT COALESCE(AVG(rating), 0) FROM review WHERE movie_id = #{movieId}")
    Double getAverageRating(Long movieId);

    @Select("SELECT COUNT(*) FROM review WHERE movie_id = #{movieId}")
    Integer getRatingCount(Long movieId);

    @Select("SELECT COALESCE(AVG(rating), 0) FROM review WHERE movie_id = #{movieId} AND created_at >= #{startDate} AND created_at < #{endDate}")
    Double getAverageRatingByDate(@Param("movieId") Long movieId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM review WHERE movie_id = #{movieId} AND created_at >= #{startDate} AND created_at < #{endDate}")
    Integer getRatingCountByDate(@Param("movieId") Long movieId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
