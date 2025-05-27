package com.example.consumer.client;

import org.springframework.stereotype.Component;

/**
 * ProviderClient的服务降级实现
 * 用于断路器生效时返回备用响应
 */
@Component("providerClientFallbackImpl")
public class ProviderClientFallback implements ProviderClient {

    @Override
    public String hello() {
        // 确保返回的是非null值，防止空指针异常
        return "Provider服务不可用，这是断路器A的降级响应 (安全返回)";
    }
} 