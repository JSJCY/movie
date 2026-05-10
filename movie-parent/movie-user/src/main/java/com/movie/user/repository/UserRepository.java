package com.movie.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.movie.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRepository extends BaseMapper<User> {
}
