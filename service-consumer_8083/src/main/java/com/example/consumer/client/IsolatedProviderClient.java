package com.example.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 带隔离保护的服务提供者客户端
 * 用于展示如何直接在Feign客户端级别应用隔离保护
 */
@FeignClient(
    name = "service-provider",
    contextId = "isolatedProviderClient", 
    fallback = IsolatedProviderClientFallback.class,
    url = "${provider.service.url:http://localhost:8081}"
)
public interface IsolatedProviderClient {

    /**
     * 获取问候消息
     */
    @GetMapping("/provider/hello")
    String getHello();
    
    /**
     * 模拟一个高负载的长时间运行的API调用
     */
    @GetMapping("/provider/heavy-task/{taskId}")
    String performHeavyTask(@PathVariable("taskId") String taskId);
} 