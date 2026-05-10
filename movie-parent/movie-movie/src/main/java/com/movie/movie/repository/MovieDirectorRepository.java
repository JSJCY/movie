package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.MovieDirector;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieDirectorRepository extends BaseMapper<MovieDirector> {
}
