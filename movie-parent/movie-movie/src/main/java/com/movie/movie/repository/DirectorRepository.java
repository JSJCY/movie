package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.Director;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DirectorRepository extends BaseMapper<Director> {
}
