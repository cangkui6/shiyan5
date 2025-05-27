package com.example.consumer.controller;

import com.example.consumer.client.FlowControlClient;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 流量控制测试控制器
 * 专门用于测试限流器功能，与其他弹性组件完全隔离
 */
@RestController
@RequestMapping("/consumer/flow-control")
public class FlowControlController {
    
    private static final Logger log = LoggerFactory.getLogger(FlowControlController.class);
    
    @Autowired
    private FlowControlClient flowControlClient;
    
    /**
     * 限流测试 - 基本接口
     */
    @GetMapping("/hello")
    @RateLimiter(name = "rateLimit2023", fallbackMethod = "helloFallback")
    public ResponseEntity<String> getHello() {
        long startTime = System.currentTimeMillis();
        log.info("流量控制测试 - 开始请求hello, 时间: {}", startTime);
        
        String result = flowControlClient.getHello();
        
        log.info("流量控制测试 - 请求完成, 耗时: {}ms", System.currentTimeMillis() - startTime);
        return ResponseEntity.ok("流量控制测试结果: " + result);
    }
    
    /**
     * 限流测试 - 任务处理接口
     */
    @GetMapping("/task/{taskId}")
    @RateLimiter(name = "rateLimit2023", fallbackMethod = "taskFallback")
    public ResponseEntity<String> processTask(@PathVariable String taskId) {
        long startTime = System.currentTimeMillis();
        log.info("流量控制测试 - 开始处理任务, ID: {}, 时间: {}", taskId, startTime);
        
        String result = flowControlClient.processTask(taskId);
        
        log.info("流量控制测试 - 任务处理完成, ID: {}, 耗时: {}ms", 
                taskId, System.currentTimeMillis() - startTime);
        return ResponseEntity.ok("流量控制任务结果: " + result);
    }
    
    /**
     * hello接口限流降级方法
     */
    public ResponseEntity<String> helloFallback(Exception ex) {
        log.warn("限流降级 - hello接口: {}", ex.getMessage());
        return ResponseEntity.ok("请求频率超限! 每2秒仅允许5个请求 [限流降级]");
    }
    
    /**
     * 任务处理接口限流降级方法
     */
    public ResponseEntity<String> taskFallback(String taskId, Exception ex) {
        log.warn("限流降级 - 任务处理 taskId: {}, 错误: {}", taskId, ex.getMessage());
        return ResponseEntity.ok("任务处理请求频率超限! 每2秒仅允许5个请求 [限流降级], 任务ID: " + taskId);
    }
} 