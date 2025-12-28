package com.hsx.manyue.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 定时任务配置
 * 用于启用Spring的定时任务功能
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Spring Boot会自动扫描带有@Scheduled注解的方法
}
