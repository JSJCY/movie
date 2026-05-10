package com.movie.ranking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.movie.ranking", "com.movie.common"})
@EnableDiscoveryClient
@EnableScheduling
@MapperScan("com.movie.ranking.repository")
public class RankingApplication {
    public static void main(String[] args) {
        SpringApplication.run(RankingApplication.class, args);
    }
}
