package com.example.consumer.controller;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 用于专门测试隔离器功能的控制器
 */
@RestController
@RequestMapping("/consumer/bulkhead-test")
public class BulkheadTestController {
    
    private static final Logger log = LoggerFactory.getLogger(BulkheadTestController.class);
    
    /**
     * 测试隔离器模拟长时间运行的操作
     * 此API会模拟一个耗时的操作，睡眠指定的秒数
     * 
     * @param seconds 睡眠的秒数
     * @return 响应结果
     */
    @GetMapping("/sleep/{seconds}")
    @Bulkhead(name = "userServiceBulkhead", fallbackMethod = "sleepFallback")
    public ResponseEntity<String> simulateLongOperation(@PathVariable("seconds") int seconds) {
        log.info("开始执行耗时操作，将持续{}秒", seconds);
        try {
            // 模拟耗时操作
            TimeUnit.SECONDS.sleep(seconds);
            
            log.info("耗时操作完成");
            return ResponseEntity.ok("操作完成，用时" + seconds + "秒");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("操作被中断", e);
            return ResponseEntity.ok("操作被中断");
        }
    }
    
    /**
     * 隔离器降级方法
     */
    public ResponseEntity<String> sleepFallback(int seconds, Exception ex) {
        log.warn("隔离器拒绝请求，当前并发数已达到上限");
        return ResponseEntity.ok("系统繁忙，请稍后再试。当前系统正在处理的请求数已达到上限 [隔离器工作中]");
    }
    
    /**
     * 测试隔离器并返回当前线程信息
     */
    @GetMapping("/info")
    @Bulkhead(name = "userServiceBulkhead", fallbackMethod = "infoFallback")
    public ResponseEntity<String> threadInfo() {
        Thread currentThread = Thread.currentThread();
        String info = String.format(
            "线程ID: %d, 线程名称: %s, 线程优先级: %d, 线程状态: %s",
            currentThread.threadId(),
            currentThread.getName(),
            currentThread.getPriority(),
            currentThread.getState()
        );
        
        log.info("处理请求, {}", info);
        return ResponseEntity.ok(info);
    }
    
    /**
     * 线程信息请求的降级方法
     */
    public ResponseEntity<String> infoFallback(Exception ex) {
        log.warn("隔离器拒绝线程信息请求");
        return ResponseEntity.ok("隔离器拒绝请求，当前并发数已达到上限");
    }
} 