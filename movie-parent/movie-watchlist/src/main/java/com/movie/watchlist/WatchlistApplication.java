package com.movie.watchlist;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(scanBasePackages = {"com.movie.watchlist", "com.movie.common"})
@EnableDiscoveryClient
@MapperScan("com.movie.watchlist.repository")
public class WatchlistApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatchlistApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
