package com.movie.watchlist.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.watchlist.entity.WatchRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WatchRecordRepository extends BaseMapper<WatchRecord> {
}
