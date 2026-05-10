package com.movie.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.movie.review", "com.movie.common"})
@EnableDiscoveryClient
@MapperScan("com.movie.review.repository")
public class ReviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReviewApplication.class, args);
    }
}
