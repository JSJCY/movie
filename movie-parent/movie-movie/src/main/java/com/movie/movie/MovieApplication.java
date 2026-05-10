package com.movie.movie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.movie.movie", "com.movie.common"})
@EnableDiscoveryClient
@MapperScan("com.movie.movie.repository")
public class MovieApplication {
    public static void main(String[] args) {
        SpringApplication.run(MovieApplication.class, args);
    }
}
