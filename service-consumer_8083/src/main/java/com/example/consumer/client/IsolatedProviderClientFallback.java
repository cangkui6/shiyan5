package com.example.consumer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * IsolatedProviderClient的降级实现
 */
@Component
public class IsolatedProviderClientFallback implements IsolatedProviderClient {

    private static final Logger log = LoggerFactory.getLogger(IsolatedProviderClientFallback.class);

    @Override
    public String getHello() {
        log.warn("Fallback: getHello");
        return "服务不可用，这是基于Feign集成的降级响应";
    }

    @Override
    public String performHeavyTask(String taskId) {
        log.warn("Fallback: performHeavyTask, taskId: {}", taskId);
        return "繁重任务执行失败，这是基于Feign集成的降级响应";
    }
} 