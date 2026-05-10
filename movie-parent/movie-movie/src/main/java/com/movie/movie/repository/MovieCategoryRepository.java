package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.MovieCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MovieCategoryRepository extends BaseMapper<MovieCategory> {
}
