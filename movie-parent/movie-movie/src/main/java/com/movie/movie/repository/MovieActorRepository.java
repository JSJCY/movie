package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.MovieActor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieActorRepository extends BaseMapper<MovieActor> {
}
