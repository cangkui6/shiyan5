package com.example.consumer.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 配置类
 * 可以在此处以编程方式配置各种弹性组件
 */
@Configuration
public class ResilienceConfig {
    
    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    /**
     * 创建一个BulkheadRegistry，可以用于监控和管理所有隔离器
     */
    @Bean
    public BulkheadRegistry bulkheadRegistry() {
        // 创建隔离器配置
        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(10)                     // 最大并发调用数量
                .maxWaitDuration(Duration.ofMillis(20))     // 等待获取许可的最大时间
                // 注意：根据实际支持的方法调整配置
                .writableStackTraceEnabled(true)            // 启用可写堆栈跟踪
                .build();

        // 创建隔离器注册表
        BulkheadRegistry registry = BulkheadRegistry.of(bulkheadConfig);
        
        // 创建一个命名为userServiceBulkhead的隔离器实例（通常会从application.yml加载配置）
        Bulkhead bulkhead = registry.bulkhead("userServiceBulkhead");
        
        // 添加事件监听器
        bulkhead.getEventPublisher()
                .onCallPermitted(event -> log.info("隔离器允许调用: {}", event))
                .onCallRejected(event -> log.warn("隔离器拒绝调用: {}", event))
                .onCallFinished(event -> log.info("隔离器调用完成: {}", event));

        return registry;
    }
} 