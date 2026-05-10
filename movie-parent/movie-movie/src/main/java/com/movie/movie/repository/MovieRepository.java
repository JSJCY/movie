package com.movie.movie.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.movie.entity.Movie;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MovieRepository extends BaseMapper<Movie> {

    @Select("SELECT * FROM movie WHERE status = 1 AND MATCH(title, description) AGAINST(#{keyword} IN BOOLEAN MODE)")
    List<Movie> fullTextSearch(String keyword);
}
