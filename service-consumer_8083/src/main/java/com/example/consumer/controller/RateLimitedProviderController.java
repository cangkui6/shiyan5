package com.example.consumer.controller;

import com.example.consumer.client.RateLimitedProviderClient;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 使用限流器保护的服务提供者控制器
 * 专门用于展示限流器在OpenFeign客户端上的应用
 */
@RestController
@RequestMapping("/consumer/rate-limited-provider")
public class RateLimitedProviderController {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitedProviderController.class);
    
    @Autowired
    private RateLimitedProviderClient rateLimitedProviderClient;
    
    /**
     * 获取问候信息 - 受限流器保护
     */
    @GetMapping("/hello")
    @RateLimiter(name = "providerServiceRateLimiter", fallbackMethod = "helloFallback")
    public ResponseEntity<String> getHello() {
        long startTime = System.currentTimeMillis();
        log.info("【限流测试】开始处理hello请求, 时间戳: {}", startTime);
        
        try {
            String result = rateLimitedProviderClient.getHello();
            long duration = System.currentTimeMillis() - startTime;
            log.info("【限流测试】hello请求处理完成, 耗时: {}ms, 结果长度: {}", duration, result.length());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("【限流测试】hello请求处理异常: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 执行繁重任务 - 受限流器保护
     */
    @GetMapping("/heavy-task/{taskId}")
    @RateLimiter(name = "providerServiceRateLimiter", fallbackMethod = "heavyTaskFallback")
    public ResponseEntity<String> performHeavyTask(@PathVariable String taskId) {
        long startTime = System.currentTimeMillis();
        log.info("【限流测试】开始处理heavy-task请求, taskId: {}, 时间戳: {}", taskId, startTime);
        
        try {
            String result = rateLimitedProviderClient.performHeavyTask(taskId);
            long duration = System.currentTimeMillis() - startTime;
            log.info("【限流测试】heavy-task请求处理完成, taskId: {}, 耗时: {}ms", taskId, duration);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("【限流测试】heavy-task请求处理异常, taskId: {}, 错误: {}", taskId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 限流器降级方法 - 用于hello接口
     */
    public ResponseEntity<String> helloFallback(Exception ex) {
        log.warn("【限流测试】限流器触发，拒绝hello请求: {}", ex.getMessage());
        return ResponseEntity.ok("请求频率过高，被限流器拒绝 [限流降级]");
    }
    
    /**
     * 限流器降级方法 - 用于繁重任务
     */
    public ResponseEntity<String> heavyTaskFallback(String taskId, Exception ex) {
        log.warn("【限流测试】限流器触发，拒绝繁重任务请求, taskId: {}, error: {}", taskId, ex.getMessage());
        return ResponseEntity.ok("请求频率过高，繁重任务请求被限流器拒绝 [限流降级]");
    }
} 