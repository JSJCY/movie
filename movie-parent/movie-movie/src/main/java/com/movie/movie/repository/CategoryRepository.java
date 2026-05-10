package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryRepository extends BaseMapper<Category> {
}
