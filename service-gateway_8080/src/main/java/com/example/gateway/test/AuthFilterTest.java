package com.example.gateway.test;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 认证过滤器测试类
 * 用于测试Gateway的全局认证过滤器功能
 */
public class AuthFilterTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String VALID_TOKEN = "valid-token-123456";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        System.out.println("=== 开始测试Gateway的全局认证过滤器 ===");
        
        // 1. 测试未提供token的情况
        testNoToken();
        
        // 2. 测试提供无效token的情况
        testInvalidToken();
        
        // 3. 测试提供有效token的情况
        testValidToken();
        
        // 4. 测试白名单路径
        testWhitelistPath();
        
        // 5. 测试登录接口
        testLogin();
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    /**
     * 测试未提供token的情况
     */
    private static void testNoToken() {
        System.out.println("\n1. 未提供token的情况 - 应当返回401未授权错误");
        System.out.println("-----------------------------------------------------");
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(BASE_URL + "/provider/hello", String.class);
            System.out.println("响应码: " + response.getStatusCode());
            System.out.println("响应内容: " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("响应码: " + e.getStatusCode());
            System.out.println("响应内容: " + e.getResponseBodyAsString());
        }
    }
    
    /**
     * 测试提供无效token的情况
     */
    private static void testInvalidToken() {
        System.out.println("\n2. 提供无效token的情况 - 应当返回401未授权错误");
        System.out.println("-----------------------------------------------------");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", "invalid-token");
            
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/provider/hello", 
                    HttpMethod.GET, 
                    entity, 
                    String.class);
            
            System.out.println("响应码: " + response.getStatusCode());
            System.out.println("响应内容: " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("响应码: " + e.getStatusCode());
            System.out.println("响应内容: " + e.getResponseBodyAsString());
        }
    }
    
    /**
     * 测试提供有效token的情况
     */
    private static void testValidToken() {
        System.out.println("\n3. 提供有效token的情况 - 应当成功访问");
        System.out.println("-----------------------------------------------------");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Auth-Token", VALID_TOKEN);
            
            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/provider/hello", 
                    HttpMethod.GET, 
                    entity, 
                    String.class);
            
            System.out.println("响应码: " + response.getStatusCode());
            System.out.println("响应内容: " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("响应码: " + e.getStatusCode());
            System.out.println("响应内容: " + e.getResponseBodyAsString());
        }
    }
    
    /**
     * 测试白名单路径
     */
    private static void testWhitelistPath() {
        System.out.println("\n4. 访问白名单路径（无需认证）");
        System.out.println("-----------------------------------------------------");
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(BASE_URL + "/public-api", Map.class);
            System.out.println("响应码: " + response.getStatusCode());
            System.out.println("响应内容: " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("响应码: " + e.getStatusCode());
            System.out.println("响应内容: " + e.getResponseBodyAsString());
        }
    }
    
    /**
     * 测试登录接口
     */
    private static void testLogin() {
        System.out.println("\n5. 模拟登录获取token");
        System.out.println("-----------------------------------------------------");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", "admin");
            map.add("password", "password");
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/login",
                    HttpMethod.POST,
                    entity,
                    Map.class);
            
            System.out.println("响应码: " + response.getStatusCode());
            System.out.println("响应内容: " + response.getBody());
        } catch (HttpClientErrorException e) {
            System.out.println("响应码: " + e.getStatusCode());
            System.out.println("响应内容: " + e.getResponseBodyAsString());
        }
    }
} 