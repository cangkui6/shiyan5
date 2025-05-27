package com.example.consumer.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.common.ratelimiter.configuration.RateLimiterConfigCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class RateLimiterConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimiterConfiguration.class);
    
    /**
     * 自定义配置限流器
     */
    @Bean
    public RateLimiterConfigCustomizer providerServiceRateLimiterCustomizer() {
        log.info("【限流配置】正在配置providerServiceRateLimiter: 每周期5个请求, 刷新周期2秒");
        
        return RateLimiterConfigCustomizer.of("providerServiceRateLimiter", 
            builder -> builder
                .limitForPeriod(5)  // 每个周期最多处理5个请求
                .limitRefreshPeriod(Duration.ofSeconds(2))  // 刷新周期为2秒
                .timeoutDuration(Duration.ofSeconds(1)));  // 等待权限的超时时间为1秒
    }
    
    /**
     * 添加日志记录，监听限流事件
     */
    @Bean
    public Object rateLimiterEventLogger(RateLimiterRegistry rateLimiterRegistry) {
        log.info("【限流配置】注册限流器事件监听器");
        
        rateLimiterRegistry.getAllRateLimiters().forEach(rateLimiter -> 
            rateLimiter.getEventPublisher()
                .onSuccess(event -> log.info("【限流事件】请求通过: {}, 时间: {}", 
                    event.getRateLimiterName(), event.getCreationTime()))
                .onFailure(event -> log.warn("【限流事件】请求被拒: {}, 时间: {}", 
                    event.getRateLimiterName(), event.getCreationTime()))
        );
            
        return new Object();
    }
} 