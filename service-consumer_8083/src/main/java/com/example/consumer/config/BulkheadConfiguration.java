package com.example.consumer.config;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.common.bulkhead.configuration.BulkheadConfigCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BulkheadConfiguration {

    private static final Logger log = LoggerFactory.getLogger(BulkheadConfiguration.class);

    /**
     * 自定义配置隔离器
     */
    @Bean
    public BulkheadConfigCustomizer providerServiceBulkheadCustomizer() {
        return BulkheadConfigCustomizer.of("providerServiceBulkhead", 
            builder -> builder
                .maxConcurrentCalls(10)  // 允许10个并发调用
                .maxWaitDuration(java.time.Duration.ofMillis(20))  // 设置最大等待时间为20ms
                .writableStackTraceEnabled(true));  // 启用堆栈跟踪
    }
    
    /**
     * 添加日志记录
     */
    @Bean
    public Object bulkheadEventLogger(BulkheadRegistry bulkheadRegistry) {
        bulkheadRegistry.getAllBulkheads().forEach(bulkhead -> 
            bulkhead.getEventPublisher()
                .onCallPermitted(event -> log.debug("隔离器允许调用: {}", event.getBulkheadName()))
                .onCallRejected(event -> log.warn("隔离器拒绝调用: {}", event.getBulkheadName()))
                .onCallFinished(event -> log.debug("隔离器调用完成: {}", event.getBulkheadName())));
            
        return new Object();
    }
} 