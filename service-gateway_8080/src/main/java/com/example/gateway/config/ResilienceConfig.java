package com.example.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 容错机制配置类
 * 配置熔断器和时间限制器
 */
@Configuration
public class ResilienceConfig {
    
    /**
     * 配置熔断器工厂
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10) // 滑动窗口大小（请求数量）
                        .failureRateThreshold(50) // 失败率阈值（百分比）
                        .waitDurationInOpenState(Duration.ofSeconds(10)) // 熔断后等待恢复的时间
                        .permittedNumberOfCallsInHalfOpenState(5) // 半开状态允许的请求数
                        .slowCallRateThreshold(50) // 慢调用率阈值（百分比）
                        .slowCallDurationThreshold(Duration.ofSeconds(2)) // 慢调用时长阈值
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(3)) // 超时时间
                        .build())
                .build());
    }

    /**
     * 配置特定服务的熔断器
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> specificCustomizer() {
        return factory -> {
            // 为provider服务配置特殊的熔断策略
            factory.configure(builder -> builder
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(5)
                            .failureRateThreshold(40)
                            .waitDurationInOpenState(Duration.ofSeconds(20))
                            .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(2))
                            .build()), "service-provider");
            
            // 为consumer服务配置特殊的熔断策略
            factory.configure(builder -> builder
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(8)
                            .failureRateThreshold(60)
                            .waitDurationInOpenState(Duration.ofSeconds(15))
                            .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(4))
                            .build()), "service-consumer");
        };
    }
    
    /**
     * 创建熔断器注册表
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build());
    }
} 