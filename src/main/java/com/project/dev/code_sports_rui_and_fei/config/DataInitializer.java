package com.project.dev.code_sports_rui_and_fei.config;

import com.project.dev.code_sports_rui_and_fei.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 应用首次启动时的初始化逻辑
 * 用户通过 /api/auth/register 自行注册
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        long count = userRepository.count();
        log.info("当前用户数: {}", count);
        if (count == 0) {
            log.info("系统已就绪，请通过 /api/auth/register 注册账号");
        }
    }
}
