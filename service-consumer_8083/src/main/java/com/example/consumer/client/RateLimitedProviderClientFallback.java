package com.example.consumer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * RateLimitedProviderClient的降级实现
 */
@Component
public class RateLimitedProviderClientFallback implements RateLimitedProviderClient {

    private static final Logger log = LoggerFactory.getLogger(RateLimitedProviderClientFallback.class);

    @Override
    public String getHello() {
        log.warn("限流降级: getHello");
        return "服务被限流，这是Feign客户端的降级响应";
    }

    @Override
    public String performHeavyTask(String taskId) {
        log.warn("限流降级: performHeavyTask, taskId: {}", taskId);
        return "繁重任务被限流，这是Feign客户端的降级响应，任务ID: " + taskId;
    }
} 