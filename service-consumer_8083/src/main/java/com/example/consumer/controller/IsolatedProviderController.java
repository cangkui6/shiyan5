package com.example.consumer.controller;

import com.example.consumer.client.IsolatedProviderClient;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 使用隔离器保护的服务提供者控制器
 * 专门用于展示隔离器在OpenFeign客户端上的应用
 */
@RestController
@RequestMapping("/consumer/isolated-provider")
public class IsolatedProviderController {
    
    private static final Logger log = LoggerFactory.getLogger(IsolatedProviderController.class);
    
    @Autowired
    private IsolatedProviderClient isolatedProviderClient;
    
    /**
     * 获取问候信息 - 受隔离器保护
     */
    @GetMapping("/hello")
    @Bulkhead(name = "providerServiceBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "helloFallback")
    public ResponseEntity<String> getHello() {
        log.info("通过隔离保护的客户端调用hello接口");
        return ResponseEntity.ok(isolatedProviderClient.getHello());
    }
    
    /**
     * 执行繁重任务 - 受隔离器保护
     */
    @GetMapping("/heavy-task/{taskId}")
    @Bulkhead(name = "providerServiceBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "heavyTaskFallback")
    public ResponseEntity<String> performHeavyTask(@PathVariable String taskId) {
        log.info("通过隔离保护的客户端执行繁重任务, taskId: {}", taskId);
        return ResponseEntity.ok(isolatedProviderClient.performHeavyTask(taskId));
    }
    
    /**
     * 隔离器降级方法 - 用于hello接口
     * 必须匹配原方法的签名，并添加Throwable参数
     */
    public ResponseEntity<String> helloFallback(Throwable ex) {
        log.warn("隔离器触发，拒绝hello请求: {}", ex.getMessage());
        return ResponseEntity.ok("资源受限，请求被隔离器拒绝 [隔离降级]");
    }
    
    /**
     * 隔离器降级方法 - 用于繁重任务
     * 必须匹配原方法的签名，并添加Throwable参数
     */
    public ResponseEntity<String> heavyTaskFallback(String taskId, Throwable ex) {
        log.warn("隔离器触发，拒绝繁重任务请求, taskId: {}, error: {}", taskId, ex.getMessage());
        return ResponseEntity.ok("资源受限，繁重任务请求被隔离器拒绝 [隔离降级]");
    }
} 