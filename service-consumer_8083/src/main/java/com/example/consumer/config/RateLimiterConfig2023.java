package com.example.consumer.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

/**
 * 限流器配置类 - 实现流量控制
 * 独立配置，确保与其他弹性组件不互相干扰
 */
@Configuration
public class RateLimiterConfig2023 {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfig2023.class);
    
    /**
     * 自定义配置限流器
     * 每2秒允许5个请求通过
     */
    @Bean
    public RateLimiterConfigCustomizer rateLimiterCustomizer2023() {
        log.info("配置限流器: rateLimit2023，设置每2秒5个请求的限制");
        
        return RateLimiterConfigCustomizer.of("rateLimit2023", 
            builder -> builder
                .limitForPeriod(5)           // 每个周期最多处理5个请求
                .limitRefreshPeriod(Duration.ofSeconds(2))  // 刷新周期为2秒
                .timeoutDuration(Duration.ofMillis(500)));  // 等待超时时间
    }
    
    /**
     * 监听限流事件
     */
    @Bean
    public Object rateLimiterEventLogger2023(RateLimiterRegistry rateLimiterRegistry) {
        log.info("注册限流器(rateLimit2023)事件监听器");
        
        rateLimiterRegistry.getAllRateLimiters().forEach(rateLimiter -> {
            if ("rateLimit2023".equals(rateLimiter.getName())) {
                rateLimiter.getEventPublisher()
                    .onSuccess(event -> log.info("限流通过: {}, 时间: {}", 
                        event.getRateLimiterName(), event.getCreationTime()))
                    .onFailure(event -> log.warn("限流拒绝: {}, 时间: {}", 
                        event.getRateLimiterName(), event.getCreationTime()));
            }
        });
            
        return new Object();
    }
} 