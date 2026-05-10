package com.movie.ranking.controller;

import com.movie.common.dto.ApiResponse;
import com.movie.ranking.dto.RankingVO;
import com.movie.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/weekly")
    public ApiResponse<List<RankingVO>> getWeekly(
            @RequestParam(required = false) String period) {
        List<RankingVO> rankings = rankingService.getWeeklyRanking(period);
        return ApiResponse.ok(rankings);
    }

    @GetMapping("/monthly")
    public ApiResponse<List<RankingVO>> getMonthly(
            @RequestParam(required = false) String period) {
        List<RankingVO> rankings = rankingService.getMonthlyRanking(period);
        return ApiResponse.ok(rankings);
    }
}
