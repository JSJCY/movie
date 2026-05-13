package com.movie.ranking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.movie.common.client.MovieClient;
import com.movie.common.client.ReviewClient;
import com.movie.common.client.WatchlistClient;
import com.movie.common.dto.ApiResponse;
import com.movie.common.dto.MovieBriefDTO;
import com.movie.common.dto.MovieSummaryDTO;
import com.movie.common.dto.PageResult;
import com.movie.common.dto.ReviewStatsDTO;
import com.movie.common.exception.BusinessException;
import com.movie.ranking.dto.RankingVO;
import com.movie.ranking.entity.RankingSnapshot;
import com.movie.ranking.repository.RankingSnapshotRepository;
import com.movie.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private static final int BAYESIAN_THRESHOLD_M = 5;
    private static final int TOP_N = 50;

    private final RankingSnapshotRepository rankingSnapshotRepository;
    private final MovieClient movieClient;
    private final ReviewClient reviewClient;
    private final WatchlistClient watchlistClient;

    @Override
    public List<RankingVO> getWeeklyRanking(String period) {
        LocalDate periodEnd;
        if (period != null && !period.isBlank()) {
            try {
                int year = Integer.parseInt(period.substring(0, 4));
                int week = Integer.parseInt(period.substring(5));
                WeekFields wf = WeekFields.ISO;
                periodEnd = LocalDate.ofYearDay(year, 1)
                        .with(wf.weekOfYear(), week)
                        .with(wf.dayOfWeek(), 7);
            } catch (Exception e) {
                throw new BusinessException(400, "period 格式错误，正确格式: 2026-W19");
            }
        } else {
            periodEnd = LocalDate.now().with(DayOfWeek.SUNDAY);
        }
        LocalDate periodStart = periodEnd.minusDays(6);

        return queryRanking("WEEKLY", periodEnd);
    }

    @Override
    public List<RankingVO> getMonthlyRanking(String period) {
        LocalDate periodEnd;
        if (period != null && !period.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(period);
                periodEnd = ym.atEndOfMonth();
            } catch (Exception e) {
                throw new BusinessException(400, "period 格式错误，正确格式: 2026-05");
            }
        } else {
            periodEnd = YearMonth.now().atEndOfMonth();
        }

        return queryRanking("MONTHLY", periodEnd);
    }

    @Override
    @Transactional
    public void calculateWeeklyRanking() {
        log.info("开始计算周排行榜...");
        LocalDate today = LocalDate.now();
        LocalDate periodEnd = today.with(DayOfWeek.SUNDAY);
        LocalDate periodStart = periodEnd.minusDays(6);

        LocalDate apiStart = periodStart;
        LocalDate apiEnd = periodEnd.plusDays(1);

        List<MovieStat> stats = fetchAllMovieStats(apiStart, apiEnd);
        List<RankingSnapshot> snapshots = buildSnapshots(stats, "WEEKLY", periodStart, periodEnd);

        rankingSnapshotRepository.delete(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getPeriodType, "WEEKLY")
                        .eq(RankingSnapshot::getPeriodEnd, periodEnd));

        for (RankingSnapshot s : snapshots) {
            rankingSnapshotRepository.insert(s);
        }

        log.info("周排行榜计算完成: {} ~ {}, 共 {} 条", periodStart, periodEnd, snapshots.size());
    }

    @Override
    @Transactional
    public void calculateMonthlyRanking() {
        log.info("开始计算月排行榜...");
        YearMonth ym = YearMonth.now();
        LocalDate periodEnd = ym.atEndOfMonth();
        LocalDate periodStart = ym.atDay(1);

        LocalDate apiStart = periodStart;
        LocalDate apiEnd = periodEnd.plusDays(1);

        List<MovieStat> stats = fetchAllMovieStats(apiStart, apiEnd);
        List<RankingSnapshot> snapshots = buildSnapshots(stats, "MONTHLY", periodStart, periodEnd);

        rankingSnapshotRepository.delete(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getPeriodType, "MONTHLY")
                        .eq(RankingSnapshot::getPeriodEnd, periodEnd));

        for (RankingSnapshot s : snapshots) {
            rankingSnapshotRepository.insert(s);
        }

        log.info("月排行榜计算完成: {} ~ {}, 共 {} 条", periodStart, periodEnd, snapshots.size());
    }

    // ==================== 内部方法 ====================

    private List<MovieStat> fetchAllMovieStats(LocalDate startDate, LocalDate endDate) {
        List<Long> movieIds = fetchAllMovieIds();
        if (movieIds.isEmpty()) {
            log.warn("没有找到任何电影");
            return List.of();
        }

        log.info("开始聚合 {} 部电影的统计数据...", movieIds.size());
        List<MovieStat> stats = new ArrayList<>();

        for (Long movieId : movieIds) {
            try {
                BigDecimal avgRating = fetchReviewAvg(movieId, startDate, endDate);
                Integer reviewCount = fetchReviewCount(movieId, startDate, endDate);
                Integer watchCount = fetchWatchCount(movieId, startDate, endDate);

                if (reviewCount > 0 || watchCount > 0) {
                    stats.add(new MovieStat(movieId, avgRating, reviewCount, watchCount));
                }
            } catch (Exception e) {
                log.warn("获取电影 {} 统计失败: {}", movieId, e.getMessage());
            }
        }

        log.info("成功聚合 {} 部电影的统计数据", stats.size());
        return stats;
    }

    private List<Long> fetchAllMovieIds() {
        try {
            ApiResponse<PageResult<MovieSummaryDTO>> countResp = movieClient.listMovies(1, 1);
            if (countResp == null || countResp.getCode() != 200 || countResp.getData() == null) {
                log.error("获取电影总数失败");
                return List.of();
            }
            long total = countResp.getData().getTotal();
            if (total == 0) {
                return List.of();
            }

            ApiResponse<PageResult<MovieSummaryDTO>> allResp = movieClient.listMovies(1, total);
            if (allResp == null || allResp.getCode() != 200 || allResp.getData() == null) {
                log.error("获取电影列表失败");
                return List.of();
            }

            return allResp.getData().getRecords().stream()
                    .map(MovieSummaryDTO::getId)
                    .toList();
        } catch (Exception e) {
            log.error("获取电影列表异常", e);
            return List.of();
        }
    }

    private BigDecimal fetchReviewAvg(Long movieId, LocalDate startDate, LocalDate endDate) {
        try {
            ApiResponse<ReviewStatsDTO> resp = reviewClient.getStats(movieId, startDate, endDate);
            if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
                return resp.getData().getAverageRating() != null
                        ? resp.getData().getAverageRating()
                        : BigDecimal.ZERO;
            }
        } catch (Exception e) {
            log.debug("获取电影 {} 评分失败: {}", movieId, e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private Integer fetchReviewCount(Long movieId, LocalDate startDate, LocalDate endDate) {
        try {
            ApiResponse<ReviewStatsDTO> resp = reviewClient.getStats(movieId, startDate, endDate);
            if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
                return resp.getData().getRatingCount() != null
                        ? resp.getData().getRatingCount()
                        : 0;
            }
        } catch (Exception e) {
            log.debug("获取电影 {} 评分人数失败: {}", movieId, e.getMessage());
        }
        return 0;
    }

    private Integer fetchWatchCount(Long movieId, LocalDate startDate, LocalDate endDate) {
        try {
            ApiResponse<Integer> resp = watchlistClient.getWatchCount(movieId, startDate, endDate);
            if (resp != null && resp.getCode() == 200 && resp.getData() != null) {
                return resp.getData();
            }
        } catch (Exception e) {
            log.debug("获取电影 {} 观影人数失败: {}", movieId, e.getMessage());
        }
        return 0;
    }

    private List<RankingSnapshot> buildSnapshots(List<MovieStat> stats, String periodType,
                                                   LocalDate periodStart, LocalDate periodEnd) {
        if (stats.isEmpty()) {
            return List.of();
        }

        BigDecimal globalSum = BigDecimal.ZERO;
        int globalCount = 0;
        for (MovieStat s : stats) {
            if (s.reviewCount > 0) {
                globalSum = globalSum.add(s.avgRating.multiply(BigDecimal.valueOf(s.reviewCount)));
                globalCount += s.reviewCount;
            }
        }
        BigDecimal C = globalCount > 0
                ? globalSum.divide(BigDecimal.valueOf(globalCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal m = BigDecimal.valueOf(BAYESIAN_THRESHOLD_M);
        for (MovieStat s : stats) {
            BigDecimal v = BigDecimal.valueOf(s.reviewCount);
            BigDecimal R = s.avgRating;
            BigDecimal vPlusM = v.add(m);
            BigDecimal weightV = v.divide(vPlusM, 4, RoundingMode.HALF_UP);
            BigDecimal weightM = m.divide(vPlusM, 4, RoundingMode.HALF_UP);
            s.score = R.multiply(weightV).add(C.multiply(weightM));
        }

        stats.sort(Comparator.comparing((MovieStat s) -> s.score).reversed());
        List<MovieStat> topStats = stats.size() > TOP_N ? stats.subList(0, TOP_N) : stats;

        List<RankingSnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < topStats.size(); i++) {
            MovieStat s = topStats.get(i);
            RankingSnapshot snapshot = new RankingSnapshot();
            snapshot.setPeriodType(periodType);
            snapshot.setPeriodStart(periodStart);
            snapshot.setPeriodEnd(periodEnd);
            snapshot.setMovieId(s.movieId);
            snapshot.setRank(i + 1);
            snapshot.setAvgRating(s.avgRating);
            snapshot.setReviewCount(s.reviewCount);
            snapshot.setWatchCount(s.watchCount);
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    private List<RankingVO> queryRanking(String periodType, LocalDate periodEnd) {
        List<RankingSnapshot> snapshots = rankingSnapshotRepository.selectList(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getPeriodType, periodType)
                        .eq(RankingSnapshot::getPeriodEnd, periodEnd)
                        .orderByAsc(RankingSnapshot::getRank));

        List<Long> movieIds = snapshots.stream()
                .map(RankingSnapshot::getMovieId)
                .distinct()
                .toList();
        Map<Long, MovieBrief> movieMap = fetchMovieBriefs(movieIds);

        List<RankingVO> results = new ArrayList<>();
        for (RankingSnapshot s : snapshots) {
            MovieBrief brief = movieMap.get(s.getMovieId());
            results.add(RankingVO.builder()
                    .rank(s.getRank())
                    .movieId(s.getMovieId())
                    .movieTitle(brief != null ? brief.title : null)
                    .moviePoster(brief != null ? brief.posterUrl : null)
                    .avgRating(s.getAvgRating())
                    .reviewCount(s.getReviewCount())
                    .watchCount(s.getWatchCount())
                    .build());
        }
        return results;
    }

    private Map<Long, MovieBrief> fetchMovieBriefs(List<Long> movieIds) {
        if (movieIds.isEmpty()) {
            return Map.of();
        }
        try {
            ApiResponse<List<MovieBriefDTO>> resp = movieClient.getMovieBriefs(movieIds);
            if (resp == null || resp.getCode() != 200 || resp.getData() == null) {
                log.warn("批量获取电影信息失败");
                return Map.of();
            }

            Map<Long, MovieBrief> map = new HashMap<>();
            for (MovieBriefDTO dto : resp.getData()) {
                map.put(dto.getId(), new MovieBrief(dto.getTitle(), dto.getPosterUrl()));
            }
            return map;
        } catch (Exception e) {
            log.warn("调用电影批量接口异常: {}", e.getMessage());
            return Map.of();
        }
    }

    // ==================== 内部类 ====================

    private record MovieBrief(String title, String posterUrl) {}

    private static class MovieStat {
        final Long movieId;
        final BigDecimal avgRating;
        final int reviewCount;
        final int watchCount;
        BigDecimal score;

        MovieStat(Long movieId, BigDecimal avgRating, int reviewCount, int watchCount) {
            this.movieId = movieId;
            this.avgRating = avgRating;
            this.reviewCount = reviewCount;
            this.watchCount = watchCount;
        }
    }
}
