package com.movie.ranking.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.ranking.entity.RankingSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RankingSnapshotRepository extends BaseMapper<RankingSnapshot> {
}
