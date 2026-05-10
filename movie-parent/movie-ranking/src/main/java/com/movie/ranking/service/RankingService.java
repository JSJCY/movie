package com.movie.ranking.service;

import com.movie.ranking.dto.RankingVO;

import java.util.List;

public interface RankingService {
    List<RankingVO> getWeeklyRanking(String period);
    List<RankingVO> getMonthlyRanking(String period);
    void calculateWeeklyRanking();
    void calculateMonthlyRanking();
}
