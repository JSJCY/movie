package com.movie.watchlist.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.watchlist.entity.WatchRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface WatchRecordRepository extends BaseMapper<WatchRecord> {

    @Select("SELECT COUNT(*) FROM watch_record WHERE movie_id = #{movieId} AND status = 'WATCHED' AND watched_at >= #{startDate} AND watched_at < #{endDate}")
    Integer getWatchCountByDate(@Param("movieId") Long movieId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
