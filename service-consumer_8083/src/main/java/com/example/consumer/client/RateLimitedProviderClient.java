package com.example.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 带限流保护的服务提供者客户端
 * 用于展示如何通过限流器保护远程服务调用
 */
@FeignClient(
    name = "service-provider",
    contextId = "rateLimitedProviderClient",
    fallback = RateLimitedProviderClientFallback.class,
    url = "${provider.service.url:http://localhost:8081}"
)
public interface RateLimitedProviderClient {

    /**
     * 获取问候消息
     */
    @GetMapping("/provider/hello")
    String getHello();
    
    /**
     * 模拟一个需要限流的API调用
     */
    @GetMapping("/provider/heavy-task/{taskId}")
    String performHeavyTask(@PathVariable("taskId") String taskId);
} 