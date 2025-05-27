package com.example.gateway.test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 熔断器测试类
 * 用于测试Gateway的熔断保护功能
 */
public class CircuitBreakerTest {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String VALID_TOKEN = "valid-token-123456";
    private static final RestTemplate restTemplate = new RestTemplate();
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 开始测试Gateway的熔断保护功能 ===");
        
        // 1. 获取认证令牌
        String token = getAuthToken();
        
        // 2. 测试正常请求
        testNormalRequest(token);
        
        // 3. 模拟服务故障，触发熔断（连续发送多个请求，触发熔断器状态转换）
        testCircuitBreaker(token);
        
        // 4. 等待熔断器从开启状态恢复到半开状态
        waitForCircuitRecovery();
        
        // 5. 测试熔断器半开状态的请求
        testCircuitHalfOpen(token);
        
        System.out.println("\n=== 熔断器测试完成 ===");
    }
    
    /**
     * 获取认证令牌
     */
    private static String getAuthToken() {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    BASE_URL + "/login?username=admin&password=password",
                    null, Map.class);
                
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
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
     * 测试正常请求
     */
    private static void testNormalRequest(String token) {
        System.out.println("\n=== 测试正常请求 ===");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        
        try {
            // 测试API路由
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    BASE_URL + "/api/provider/hello",
                    Map.class);
                    
            System.out.println("API请求成功状态: " + response.getStatusCode());
            System.out.println("API请求响应: " + response.getBody());
        } catch (Exception e) {
            System.out.println("API请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 模拟服务故障，触发熔断
     */
    private static void testCircuitBreaker(String token) throws InterruptedException {
        System.out.println("\n=== 测试服务故障，触发熔断 ===");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        
        System.out.println("开始连续发送请求（预期部分请求会失败，最终触发熔断）...");
        
        // 发送多个请求，可能导致熔断器打开
        for (int i = 0; i < 20; i++) {
            try {
                restTemplate.getForEntity(
                        BASE_URL + "/api/provider/error", // 假设这是一个会返回错误的端点
                        Map.class);
                System.out.println("请求 #" + i + " 正常完成（不符合预期）");
            } catch (Exception e) {
                // 期望发生异常
                if (e instanceof HttpServerErrorException) {
                    HttpServerErrorException hsee = (HttpServerErrorException) e;
                    System.out.println("请求 #" + i + " 失败，状态码: " + hsee.getStatusCode());
                } else {
                    System.out.println("请求 #" + i + " 出现其他异常: " + e.getMessage());
                }
            }
            
            // 短暂等待
            Thread.sleep(100);
        }
        
        System.out.println("\n熔断器现在可能已触发...");
        
        // 检查熔断器状态 - 发起一个应该被熔断的请求
        try {
            RestTemplate newRestTemplate = new RestTemplate();
            ResponseEntity<Map> response = newRestTemplate.getForEntity(
                    BASE_URL + "/api/provider/hello",
                    Map.class);
                    
            System.out.println("熔断后API请求状态: " + response.getStatusCode());
            System.out.println("熔断后API响应: " + response.getBody());
            
            // 如果成功，则可能是回退响应
            if (response.getBody() != null && response.getBody().containsKey("code")) {
                String code = (String) response.getBody().get("code");
                if ("PROVIDER_UNAVAILABLE".equals(code)) {
                    System.out.println("确认熔断成功，已返回回退响应");
                }
            }
        } catch (Exception e) {
            System.out.println("熔断后请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 等待熔断器从开启状态恢复到半开状态
     */
    private static void waitForCircuitRecovery() throws InterruptedException {
        System.out.println("\n=== 等待熔断器恢复 ===");
        System.out.println("等待10秒，让熔断器有时间从开启状态转为半开状态...");
        Thread.sleep(10000);
    }
    
    /**
     * 测试熔断器半开状态
     */
    private static void testCircuitHalfOpen(String token) {
        System.out.println("\n=== 测试熔断器半开状态 ===");
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", token);
        
        // 熔断器半开状态下，尝试发送一些请求
        for (int i = 0; i < 3; i++) {
            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(
                        BASE_URL + "/api/provider/hello",
                        Map.class);
                        
                System.out.println("半开状态请求 #" + i + " 状态: " + response.getStatusCode());
                System.out.println("半开状态请求 #" + i + " 响应: " + response.getBody());
            } catch (Exception e) {
                System.out.println("半开状态请求 #" + i + " 失败: " + e.getMessage());
            }
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
} 