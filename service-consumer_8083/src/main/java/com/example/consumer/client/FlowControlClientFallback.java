package com.example.consumer.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 流量控制客户端的降级实现
 */
@Component
public class FlowControlClientFallback implements FlowControlClient {

    private static final Logger log = LoggerFactory.getLogger(FlowControlClientFallback.class);

    @Override
    public String getHello() {
        log.warn("流量控制 - Feign降级: getHello");
        return "服务访问受限 (Feign降级响应)";
    }

    @Override
    public String processTask(String taskId) {
        log.warn("流量控制 - Feign降级: processTask, taskId: {}", taskId);
        return "任务处理受限 (Feign降级响应), 任务ID: " + taskId;
    }
} 