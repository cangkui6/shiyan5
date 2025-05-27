package com.example.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 流量控制测试客户端
 * 专门用于限流器测试，与其他弹性组件隔离
 */
@FeignClient(
    name = "service-provider",
    contextId = "flowControlClient", 
    fallback = FlowControlClientFallback.class,
    url = "${provider.service.url:http://localhost:8081}"
)
public interface FlowControlClient {

    /**
     * 获取基本问候消息
     */
    @GetMapping("/provider/hello")
    String getHello();
    
    /**
     * 执行任务处理
     */
    @GetMapping("/provider/heavy-task/{taskId}")
    String processTask(@PathVariable("taskId") String taskId);
} 