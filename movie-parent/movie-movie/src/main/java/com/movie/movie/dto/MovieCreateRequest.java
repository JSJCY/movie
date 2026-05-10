package com.movie.movie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieCreateRequest {

    @NotBlank(message = "电影名称不能为空")
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
    private List<ActorRole> actors;
    private List<Long> directorIds;

    @Data
    public static class ActorRole {
        private Long actorId;
        private String characterName;
    }
}
