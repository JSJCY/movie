package com.movie.movie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.movie.dto.*;
import com.movie.movie.entity.*;

import java.util.List;

public interface MovieService {
    // 电影
    Page<MovieVO> listMovies(long page, long size, Long categoryId, String country, String keyword, String sortBy);
    MovieVO getMovieDetail(Long id);
    MovieVO createMovie(MovieCreateRequest request);
    MovieVO updateMovie(Long id, MovieUpdateRequest request);
    void deleteMovie(Long id);
    List<MovieVO> fullTextSearch(String keyword);

    // 分类
    List<Category> listCategories();
    Category createCategory(CategoryCreateRequest request);
    void deleteCategory(Long id);

    // 演员
    Page<Actor> listActors(long page, long size, String keyword);
    Actor createActor(ActorCreateRequest request);
    Actor updateActor(Long id, ActorCreateRequest request);
    void deleteActor(Long id);

    // 导演
    Page<Director> listDirectors(long page, long size, String keyword);
    Director createDirector(DirectorCreateRequest request);
    Director updateDirector(Long id, DirectorCreateRequest request);
    void deleteDirector(Long id);

    // TMDB 导入
    MovieVO importFromTmdb(Long tmdbId);

    // 内部调用
    Movie getById(Long id);
    List<MovieBriefVO> getMovieBriefs(List<Long> ids);
}
