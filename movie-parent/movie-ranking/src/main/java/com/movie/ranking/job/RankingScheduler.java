package com.movie.ranking.job;

import com.movie.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;

    // 每周日 23:00 计算周排行
    @Scheduled(cron = "0 0 23 * * SUN")
    public void calculateWeekly() {
        log.info("定时任务触发: 计算周排行榜");
        try {
            rankingService.calculateWeeklyRanking();
        } catch (Exception e) {
            log.error("周排行榜计算失败", e);
        }
    }

    // 每月最后一天 23:00 计算月排行
    @Scheduled(cron = "0 0 23 L * ?")
    public void calculateMonthly() {
        log.info("定时任务触发: 计算月排行榜");
        try {
            rankingService.calculateMonthlyRanking();
        } catch (Exception e) {
            log.error("月排行榜计算失败", e);
        }
    }
}
