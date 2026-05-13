package com.movie.watchlist;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.movie.watchlist", "com.movie.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.movie.common.client")
@MapperScan("com.movie.watchlist.repository")
public class WatchlistApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatchlistApplication.class, args);
    }
}
