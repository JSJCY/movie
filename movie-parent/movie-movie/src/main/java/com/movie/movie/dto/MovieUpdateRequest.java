package com.movie.movie.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieUpdateRequest {
    private String title;
    private String originalTitle;
    private LocalDate releaseDate;
    private Integer duration;
    private String country;
    private String language;
    private String description;
    private String posterUrl;
    private String trailerUrl;
    private List<Long> categoryIds;
    private List<MovieCreateRequest.ActorRole> actors;
    private List<Long> directorIds;
}
