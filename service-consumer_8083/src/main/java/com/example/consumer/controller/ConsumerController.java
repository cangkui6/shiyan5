package com.example.consumer.controller;

import com.example.consumer.client.ProviderClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/consumer")
public class ConsumerController {
    private static final Logger log = LoggerFactory.getLogger(ConsumerController.class);
    
    private final RestTemplate restTemplate;
    
    @Autowired
    private ProviderClient providerClient;  // 移除Qualifier注解，让Spring自动注入

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
     * 测试OpenFeign客户端调用
     * 用于测试Gateway路由
     */
    @GetMapping("/feign-test")
    public String feignTest() {
        log.info("Consumer服务的feign-test接口被调用");
        return "Hello from Consumer's feign-test! 正在通过Feign调用Provider: " + providerClient.hello();
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
}
