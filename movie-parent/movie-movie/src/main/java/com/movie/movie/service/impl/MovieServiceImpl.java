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

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${tmdb.api-key}")
    private String tmdbApiKey;

    @Value("${tmdb.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.image-base-url}")
    private String tmdbImageBaseUrl;

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

    @Override
    @Transactional
    public MovieVO importFromTmdb(Long tmdbId) {
        // 1. 获取 TMDB 电影详情和演职员
        JsonNode movieData = fetchTmdb("/movie/" + tmdbId + "?language=zh-CN");
        JsonNode creditsData = fetchTmdb("/movie/" + tmdbId + "/credits?language=zh-CN");

        // 2. 构建电影实体
        Movie movie = new Movie();
        movie.setTitle(getText(movieData, "title"));
        movie.setOriginalTitle(getText(movieData, "original_title"));
        String releaseDate = getText(movieData, "release_date");
        if (releaseDate != null) movie.setReleaseDate(LocalDate.parse(releaseDate));
        JsonNode runtime = movieData.get("runtime");
        if (runtime != null && !runtime.isNull()) movie.setDuration(runtime.asInt());
        JsonNode countries = movieData.get("production_countries");
        if (countries != null && countries.isArray() && countries.size() > 0) {
            movie.setCountry(countries.get(0).get("name").asText());
        }
        movie.setLanguage(getText(movieData, "original_language"));
        movie.setDescription(getText(movieData, "overview"));
        String posterPath = getText(movieData, "poster_path");
        if (posterPath != null) {
            movie.setPosterUrl(tmdbImageBaseUrl + posterPath);
        }
        movie.setStatus(1);
        movieRepository.insert(movie);

        // 3. 分类（TMDB genres）
        JsonNode genres = movieData.get("genres");
        if (genres != null) {
            for (JsonNode genre : genres) {
                String name = genre.get("name").asText();
                MovieCategory mc = new MovieCategory();
                mc.setMovieId(movie.getId());
                mc.setCategoryId(findOrCreateCategory(name).getId());
                movieCategoryRepository.insert(mc);
            }
        }

        // 4. 演员（前 10 位）
        JsonNode cast = creditsData.get("cast");
        if (cast != null) {
            int count = 0;
            for (JsonNode member : cast) {
                if (count >= 10) break;
                String name = member.get("name").asText();
                String character = member.has("character") && !member.get("character").isNull()
                        ? member.get("character").asText() : "";
                String profilePath = getText(member, "profile_path");
                MovieActor ma = new MovieActor();
                ma.setMovieId(movie.getId());
                ma.setActorId(findOrCreateActor(name, profilePath).getId());
                ma.setCharacterName(character);
                movieActorRepository.insert(ma);
                count++;
            }
        }

        // 5. 导演
        JsonNode crew = creditsData.get("crew");
        if (crew != null) {
            for (JsonNode member : crew) {
                if ("Director".equals(getText(member, "job"))) {
                    String name = member.get("name").asText();
                    String profilePath = getText(member, "profile_path");
                    MovieDirector md = new MovieDirector();
                    md.setMovieId(movie.getId());
                    md.setDirectorId(findOrCreateDirector(name, profilePath).getId());
                    movieDirectorRepository.insert(md);
                }
            }
        }

        return toMovieVO(movie);
    }

    private JsonNode fetchTmdb(String path) {
        String url = tmdbBaseUrl + path + "&api_key=" + tmdbApiKey;
        try {
            String json = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new BusinessException(500, "TMDB API 调用失败: " + e.getMessage());
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText() : null;
    }

    private Category findOrCreateCategory(String name) {
        Category cat = categoryRepository.selectOne(
                new LambdaQueryWrapper<Category>().eq(Category::getName, name));
        if (cat == null) {
            cat = new Category();
            cat.setName(name);
            categoryRepository.insert(cat);
        }
        return cat;
    }

    private Actor findOrCreateActor(String name, String profilePath) {
        Actor actor = actorRepository.selectOne(
                new LambdaQueryWrapper<Actor>().eq(Actor::getName, name));
        if (actor == null) {
            actor = new Actor();
            actor.setName(name);
            if (profilePath != null) {
                actor.setAvatarUrl("https://image.tmdb.org/t/p/w200" + profilePath);
            }
            actorRepository.insert(actor);
        }
        return actor;
    }

    private Director findOrCreateDirector(String name, String profilePath) {
        Director director = directorRepository.selectOne(
                new LambdaQueryWrapper<Director>().eq(Director::getName, name));
        if (director == null) {
            director = new Director();
            director.setName(name);
            if (profilePath != null) {
                director.setAvatarUrl("https://image.tmdb.org/t/p/w200" + profilePath);
            }
            directorRepository.insert(director);
        }
        return director;
    }

    @Override
    public List<MovieBriefVO> getMovieBriefs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return movieRepository.selectBatchIds(ids).stream()
                .map(m -> MovieBriefVO.builder()
                        .id(m.getId())
                        .title(m.getTitle())
                        .posterUrl(m.getPosterUrl())
                        .averageRating(m.getAverageRating())
                        .build())
                .toList();
    }

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
