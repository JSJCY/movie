package com.movie.user.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.movie.user.entity.User;
import com.movie.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"))) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@movie.com");
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userRepository.insert(admin);
            log.info("默认管理员账号已创建: admin / admin123");
        }
    }
}
