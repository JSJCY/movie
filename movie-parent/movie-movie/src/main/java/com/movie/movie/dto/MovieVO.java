package com.movie.movie.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MovieVO {
    private Long id;
    private String title;
    private String originalTitle;
    private LocalDate releaseDate;
    private Integer duration;
    private String country;
    private String language;
    private String description;
    private String posterUrl;
    private String trailerUrl;
    private BigDecimal averageRating;
    private Integer ratingCount;
    private List<String> categories;
    private List<ActorBrief> actors;
    private List<DirectorBrief> directors;

    @Data
    @Builder
    public static class ActorBrief {
        private Long id;
        private String name;
        private String characterName;
        private String avatarUrl;
    }

    @Data
    @Builder
    public static class DirectorBrief {
        private Long id;
        private String name;
        private String avatarUrl;
    }
}
