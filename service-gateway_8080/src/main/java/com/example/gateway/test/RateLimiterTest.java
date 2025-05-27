package com.example.gateway.test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流器测试类
 * 用于测试Gateway的全局限流功能
 */
public class RateLimiterTest {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String VALID_TOKEN = "valid-token-123456";
    private static final RestTemplate restTemplate = new RestTemplate();
    
    // 统计成功和失败的请求数量
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);
    private static final AtomicInteger limitedCount = new AtomicInteger(0);
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 开始测试Gateway的限流功能 ===");
        
        // 1. 获取认证令牌
        String token = getAuthToken();
        
        // 2. 测试正常请求（单个）
        testSingleRequest(token);
        
        // 3. 测试并发请求（触发限流）
        testConcurrentRequests(token);
        
        System.out.println("\n=== 限流测试完成 ===");
    }
    
    /**
     * 获取认证令牌
     */
    private static String getAuthToken() {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BASE_URL + "/login?username=admin&password=password",
                    null, Map.class);
                
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (Boolean.TRUE.equals(body.get("success"))) {
                    System.out.println("获取认证令牌成功: " + body.get("token"));
                    return (String) body.get("token");
                }
            }
        } catch (Exception e) {
            System.out.println("获取令牌失败: " + e.getMessage());
        }
        
        System.out.println("使用默认令牌: " + VALID_TOKEN);
        return VALID_TOKEN;
    }
    
    /**
     * 测试单个请求
     */
    private static void testSingleRequest(String token) {
        System.out.println("\n=== 测试单个请求 ===");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        
        try {
            // 测试Provider服务
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    BASE_URL + "/provider/hello",
                    Map.class);
                    
            System.out.println("单个请求状态: " + response.getStatusCode());
            System.out.println("单个请求响应: " + response.getBody());
        } catch (Exception e) {
            System.out.println("单个请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试并发请求，触发限流
     */
    private static void testConcurrentRequests(String token) throws InterruptedException {
        System.out.println("\n=== 测试并发请求，触发限流 ===");
        
        // 创建线程池
        int numThreads = 10;
        int requestsPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        System.out.println("开始发送 " + numThreads + " 个并发线程，每个线程 " + 
                requestsPerThread + " 个请求...");
        
        // 重置计数器
        successCount.set(0);
        failCount.set(0);
        limitedCount.set(0);
        
        long startTime = System.currentTimeMillis();
        
        // 提交并发任务
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    sendRequest(token);
                    // 短暂停顿
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
        
        // 关闭线程池并等待所有任务完成
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        
        // 输出统计结果
        System.out.println("\n===== 测试结果 =====");
        System.out.println("总请求数: " + (numThreads * requestsPerThread));
        System.out.println("成功请求: " + successCount.get());
        System.out.println("失败请求: " + failCount.get());
        System.out.println("被限流请求: " + limitedCount.get());
        System.out.println("测试持续时间: " + duration + " 秒");
        System.out.println("请求速率: " + (numThreads * requestsPerThread / duration) + " 请求/秒");
        
        if (limitedCount.get() > 0) {
            System.out.println("\n确认限流功能正常工作！部分请求被成功限流。");
        } else {
            System.out.println("\n未触发限流，请增加并发请求数量或减少请求间隔重试。");
        }
    }
    
    /**
     * 发送单个请求并统计结果
     */
    private static void sendRequest(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        
        try {
            restTemplate.getForEntity(
                    BASE_URL + "/provider/hello",
                    Map.class);
            
            // 请求成功
            successCount.incrementAndGet();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                // 请求被限流 (TOO_MANY_REQUESTS)
                limitedCount.incrementAndGet();
            } else {
                // 其他客户端错误
                failCount.incrementAndGet();
            }
        } catch (Exception e) {
            // 其他异常
            failCount.incrementAndGet();
        }
    }
} 