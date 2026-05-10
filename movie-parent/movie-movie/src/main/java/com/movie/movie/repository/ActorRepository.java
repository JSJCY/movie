package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.Actor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ActorRepository extends BaseMapper<Actor> {
}
