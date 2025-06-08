package com.example.consumer.controller;

import com.example.consumer.client.ProviderClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RefreshScope  // 添加RefreshScope注解，支持动态配置刷新
@RestController
@RequestMapping("/consumer")
public class ConsumerController {
    private static final Logger log = LoggerFactory.getLogger(ConsumerController.class);
    
    private final RestTemplate restTemplate;
    
    @Autowired
    private ProviderClient providerClient;  // 移除Qualifier注解，让Spring自动注入
    
    // 添加可动态刷新的配置属性
    @Value("${consumer.message:消费者默认配置消息}")
    private String configMessage;

    public ConsumerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 使用RestTemplate的原始方法，不使用断路器
     */
    @GetMapping("/hello-old")
    public String helloOld() {
        return restTemplate.getForObject("http://SERVICE-PROVIDER/provider/hello", String.class);
    }
    
    /**
     * 使用Feign客户端和断路器A进行调用
     * 当服务不可用时会触发断路器
     */
    @GetMapping("/hello")
    @CircuitBreaker(name = "circuitBreakerA", fallbackMethod = "helloFallback")
    public String hello() {
        log.info("调用provider的hello接口");
        return providerClient.hello();
    }
    
    
    /**
     * 断路器A的本地降级方法
     * 优先级高于Feign客户端的降级类
     */
    public String helloFallback(Exception ex) {
        log.error("服务调用失败", ex);
        // 检查异常消息，避免空指针
        String errorMessage = ex != null ? ex.getMessage() : "未知错误";
        return "Provider服务调用失败，这是本地断路器A的降级响应: " + errorMessage;
    }
    
    /**
     * 用于测试配置动态刷新的端点
     * 返回从配置中心获取的消息
     */
    @GetMapping("/config-message")
    public String getConfigMessage() {
        log.info("获取配置消息，当前消息：{}", configMessage);
        return "消费者的配置消息：" + configMessage;
    }
}
