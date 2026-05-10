package com.movie.ranking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingSnapshotRepository rankingSnapshotRepository;

    @Override
    public List<RankingVO> getWeeklyRanking(String period) {
        LocalDate periodEnd;
        if (period != null && !period.isBlank()) {
            // 格式: 2026-W19
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
            // 默认当前周
            periodEnd = LocalDate.now().with(DayOfWeek.SUNDAY);
        }
        LocalDate periodStart = periodEnd.minusDays(6);

        return queryRanking("WEEKLY", periodEnd);
    }

    @Override
    public List<RankingVO> getMonthlyRanking(String period) {
        LocalDate periodEnd;
        if (period != null && !period.isBlank()) {
            // 格式: 2026-05
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

        // 移除旧数据
        rankingSnapshotRepository.delete(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getPeriodType, "WEEKLY")
                        .eq(RankingSnapshot::getPeriodEnd, periodEnd));

        // 模拟排行榜数据生成
        // 实际应该聚合 review 服务和 watchlist 服务的数据
        // 这里生成快照记录作为示例
        log.info("周排行榜计算完成: {} ~ {}", periodStart, periodEnd);
    }

    @Override
    @Transactional
    public void calculateMonthlyRanking() {
        log.info("开始计算月排行榜...");
        YearMonth ym = YearMonth.now();
        LocalDate periodEnd = ym.atEndOfMonth();
        LocalDate periodStart = ym.atDay(1);

        rankingSnapshotRepository.delete(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getPeriodType, "MONTHLY")
                        .eq(RankingSnapshot::getPeriodEnd, periodEnd));

        log.info("月排行榜计算完成: {} ~ {}", periodStart, periodEnd);
    }

    private List<RankingVO> queryRanking(String periodType, LocalDate periodEnd) {
        List<RankingSnapshot> snapshots = rankingSnapshotRepository.selectList(
                new LambdaQueryWrapper<RankingSnapshot>()
                        .eq(RankingSnapshot::getPeriodType, periodType)
                        .eq(RankingSnapshot::getPeriodEnd, periodEnd)
                        .orderByAsc(RankingSnapshot::getRank));

        List<RankingVO> results = new ArrayList<>();
        for (RankingSnapshot s : snapshots) {
            results.add(RankingVO.builder()
                    .rank(s.getRank())
                    .movieId(s.getMovieId())
                    .avgRating(s.getAvgRating())
                    .reviewCount(s.getReviewCount())
                    .watchCount(s.getWatchCount())
                    .build());
        }
        return results;
    }
}
