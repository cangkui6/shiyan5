package com.example.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 服务提供者客户端接口
 * 用于调用服务提供者的基础服务
 */
@FeignClient(
    name = "service-provider", 
    contextId = "providerClient", 
    fallback = ProviderClientFallback.class,
    url = "${provider.service.url:http://localhost:8081}" // 提供默认URL，避免服务发现失败
)
@Primary
public interface ProviderClient {

    /**
     * 调用服务提供者的hello接口
     * @return 服务提供者的问候
     */
    @GetMapping("/provider/hello")
    String hello();
} 