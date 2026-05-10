package com.movie.movie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.movie.common.exception.BusinessException;
import com.movie.movie.dto.*;
import com.movie.movie.entity.*;
import com.movie.movie.repository.*;
import com.movie.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;
    private final ActorRepository actorRepository;
    private final DirectorRepository directorRepository;
    private final MovieCategoryRepository movieCategoryRepository;
    private final MovieActorRepository movieActorRepository;
    private final MovieDirectorRepository movieDirectorRepository;

    // ==================== 电影 ====================

    @Override
    public Page<MovieVO> listMovies(long page, long size, Long categoryId, String country, String keyword, String sortBy) {
        LambdaQueryWrapper<Movie> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Movie::getStatus, 1);

        if (categoryId != null) {
            // 先查关联表获取 movie_id 列表
            List<Long> movieIds = movieCategoryRepository.selectList(
                    new LambdaQueryWrapper<MovieCategory>().eq(MovieCategory::getCategoryId, categoryId)
            ).stream().map(MovieCategory::getMovieId).toList();
            if (movieIds.isEmpty()) {
                Page<MovieVO> empty = new Page<>(page, size);
                empty.setRecords(List.of());
                return empty;
            }
            wrapper.in(Movie::getId, movieIds);
        }

        if (StringUtils.hasText(country)) {
            wrapper.eq(Movie::getCountry, country);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(Movie::getTitle, keyword);
        }

        // 排序
        if ("rating".equals(sortBy)) {
            wrapper.orderByDesc(Movie::getAverageRating);
        } else if ("release_date".equals(sortBy)) {
            wrapper.orderByDesc(Movie::getReleaseDate);
        } else {
            wrapper.orderByDesc(Movie::getCreatedAt);
        }

        Page<Movie> moviePage = movieRepository.selectPage(new Page<>(page, size), wrapper);

        List<MovieVO> records = moviePage.getRecords().stream()
                .map(this::toMovieVO)
                .toList();

        Page<MovieVO> voPage = new Page<>(page, size);
        voPage.setTotal(moviePage.getTotal());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public MovieVO getMovieDetail(Long id) {
        Movie movie = getById(id);
        return toMovieVO(movie);
    }

    @Override
    @Transactional
    public MovieVO createMovie(MovieCreateRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setOriginalTitle(request.getOriginalTitle());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setDuration(request.getDuration());
        movie.setCountry(request.getCountry());
        movie.setLanguage(request.getLanguage());
        movie.setDescription(request.getDescription());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setStatus(1);
        movieRepository.insert(movie);

        // 关联分类
        if (request.getCategoryIds() != null) {
            for (Long catId : request.getCategoryIds()) {
                MovieCategory mc = new MovieCategory();
                mc.setMovieId(movie.getId());
                mc.setCategoryId(catId);
                movieCategoryRepository.insert(mc);
            }
        }

        // 关联演员
        if (request.getActors() != null) {
            for (var actorRole : request.getActors()) {
                MovieActor ma = new MovieActor();
                ma.setMovieId(movie.getId());
                ma.setActorId(actorRole.getActorId());
                ma.setCharacterName(actorRole.getCharacterName());
                movieActorRepository.insert(ma);
            }
        }

        // 关联导演
        if (request.getDirectorIds() != null) {
            for (Long dirId : request.getDirectorIds()) {
                MovieDirector md = new MovieDirector();
                md.setMovieId(movie.getId());
                md.setDirectorId(dirId);
                movieDirectorRepository.insert(md);
            }
        }

        return toMovieVO(movie);
    }

    @Override
    @Transactional
    public MovieVO updateMovie(Long id, MovieUpdateRequest request) {
        Movie movie = getById(id);

        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getOriginalTitle() != null) movie.setOriginalTitle(request.getOriginalTitle());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != null) movie.setDuration(request.getDuration());
        if (request.getCountry() != null) movie.setCountry(request.getCountry());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());

        movieRepository.updateById(movie);

        // 更新分类关联
        if (request.getCategoryIds() != null) {
            movieCategoryRepository.delete(new LambdaQueryWrapper<MovieCategory>().eq(MovieCategory::getMovieId, id));
            for (Long catId : request.getCategoryIds()) {
                MovieCategory mc = new MovieCategory();
                mc.setMovieId(id);
                mc.setCategoryId(catId);
                movieCategoryRepository.insert(mc);
            }
        }

        // 更新演员关联
        if (request.getActors() != null) {
            movieActorRepository.delete(new LambdaQueryWrapper<MovieActor>().eq(MovieActor::getMovieId, id));
            for (var ar : request.getActors()) {
                MovieActor ma = new MovieActor();
                ma.setMovieId(id);
                ma.setActorId(ar.getActorId());
                ma.setCharacterName(ar.getCharacterName());
                movieActorRepository.insert(ma);
            }
        }

        // 更新导演关联
        if (request.getDirectorIds() != null) {
            movieDirectorRepository.delete(new LambdaQueryWrapper<MovieDirector>().eq(MovieDirector::getMovieId, id));
            for (Long dirId : request.getDirectorIds()) {
                MovieDirector md = new MovieDirector();
                md.setMovieId(id);
                md.setDirectorId(dirId);
                movieDirectorRepository.insert(md);
            }
        }

        return toMovieVO(movie);
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = getById(id);
        movie.setStatus(0);
        movieRepository.updateById(movie);
    }

    @Override
    public List<MovieVO> fullTextSearch(String keyword) {
        List<Movie> movies = movieRepository.fullTextSearch(keyword);
        return movies.stream().map(this::toMovieVO).toList();
    }

    // ==================== 分类 ====================

    @Override
    public List<Category> listCategories() {
        return categoryRepository.selectList(null);
    }

    @Override
    public Category createCategory(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        categoryRepository.insert(category);
        return category;
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // ==================== 演员 ====================

    @Override
    public Page<Actor> listActors(long page, long size, String keyword) {
        LambdaQueryWrapper<Actor> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Actor::getName, keyword);
        }
        wrapper.orderByDesc(Actor::getCreatedAt);
        return actorRepository.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Actor createActor(ActorCreateRequest request) {
        Actor actor = new Actor();
        actor.setName(request.getName());
        actor.setAvatarUrl(request.getAvatarUrl());
        actor.setBio(request.getBio());
        actor.setBirthDate(request.getBirthDate());
        actor.setNationality(request.getNationality());
        actorRepository.insert(actor);
        return actor;
    }

    @Override
    public Actor updateActor(Long id, ActorCreateRequest request) {
        Actor actor = actorRepository.selectById(id);
        if (actor == null) throw new BusinessException(404, "演员不存在");
        if (request.getName() != null) actor.setName(request.getName());
        if (request.getAvatarUrl() != null) actor.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) actor.setBio(request.getBio());
        if (request.getBirthDate() != null) actor.setBirthDate(request.getBirthDate());
        if (request.getNationality() != null) actor.setNationality(request.getNationality());
        actorRepository.updateById(actor);
        return actor;
    }

    @Override
    public void deleteActor(Long id) {
        actorRepository.deleteById(id);
    }

    // ==================== 导演 ====================

    @Override
    public Page<Director> listDirectors(long page, long size, String keyword) {
        LambdaQueryWrapper<Director> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Director::getName, keyword);
        }
        wrapper.orderByDesc(Director::getCreatedAt);
        return directorRepository.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Director createDirector(DirectorCreateRequest request) {
        Director director = new Director();
        director.setName(request.getName());
        director.setAvatarUrl(request.getAvatarUrl());
        director.setBio(request.getBio());
        director.setBirthDate(request.getBirthDate());
        director.setNationality(request.getNationality());
        directorRepository.insert(director);
        return director;
    }

    @Override
    public Director updateDirector(Long id, DirectorCreateRequest request) {
        Director director = directorRepository.selectById(id);
        if (director == null) throw new BusinessException(404, "导演不存在");
        if (request.getName() != null) director.setName(request.getName());
        if (request.getAvatarUrl() != null) director.setAvatarUrl(request.getAvatarUrl());
        if (request.getBio() != null) director.setBio(request.getBio());
        if (request.getBirthDate() != null) director.setBirthDate(request.getBirthDate());
        if (request.getNationality() != null) director.setNationality(request.getNationality());
        directorRepository.updateById(director);
        return director;
    }

    @Override
    public void deleteDirector(Long id) {
        directorRepository.deleteById(id);
    }

    // ==================== 公共 ====================

    @Override
    public Movie getById(Long id) {
        Movie movie = movieRepository.selectById(id);
        if (movie == null || movie.getStatus() == 0) {
            throw new BusinessException(404, "电影不存在或已下架");
        }
        return movie;
    }

    // ==================== 内部方法 ====================

    private MovieVO toMovieVO(Movie movie) {
        // 分类
        List<String> categories = movieCategoryRepository.selectList(
                new LambdaQueryWrapper<MovieCategory>().eq(MovieCategory::getMovieId, movie.getId())
        ).stream().map(mc -> {
            Category cat = categoryRepository.selectById(mc.getCategoryId());
            return cat != null ? cat.getName() : "";
        }).filter(s -> !s.isEmpty()).toList();

        // 演员
        List<MovieVO.ActorBrief> actors = movieActorRepository.selectList(
                new LambdaQueryWrapper<MovieActor>().eq(MovieActor::getMovieId, movie.getId())
        ).stream().map(ma -> {
            Actor actor = actorRepository.selectById(ma.getActorId());
            return MovieVO.ActorBrief.builder()
                    .id(ma.getActorId())
                    .name(actor != null ? actor.getName() : "")
                    .characterName(ma.getCharacterName())
                    .avatarUrl(actor != null ? actor.getAvatarUrl() : null)
                    .build();
        }).toList();

        // 导演
        List<MovieVO.DirectorBrief> directors = movieDirectorRepository.selectList(
                new LambdaQueryWrapper<MovieDirector>().eq(MovieDirector::getMovieId, movie.getId())
        ).stream().map(md -> {
            Director dir = directorRepository.selectById(md.getDirectorId());
            return MovieVO.DirectorBrief.builder()
                    .id(md.getDirectorId())
                    .name(dir != null ? dir.getName() : "")
                    .avatarUrl(dir != null ? dir.getAvatarUrl() : null)
                    .build();
        }).toList();

        return MovieVO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .originalTitle(movie.getOriginalTitle())
                .releaseDate(movie.getReleaseDate())
                .duration(movie.getDuration())
                .country(movie.getCountry())
                .language(movie.getLanguage())
                .description(movie.getDescription())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .averageRating(movie.getAverageRating())
                .ratingCount(movie.getRatingCount())
                .categories(categories)
                .actors(actors)
                .directors(directors)
                .build();
    }
}
