package com.example.gateway.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 跨域测试控制器
 */
@RestController
@RequestMapping("/cors-test")
public class CorsTestController {

    /**
     * 公开API，无需认证
     */
    @GetMapping("/public-api")
    public Map<String, Object> publicApi() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "这是一个公开的API，无需认证 - CORS测试");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 登录API，用于获取token
     * 使用Mono处理响应式请求
     */
    @PostMapping("/login")
    public Mono<Map<String, Object>> login(ServerWebExchange exchange) {
        return exchange.getFormData()
            .flatMap(formData -> {
                String username = formData.getFirst("username");
                String password = formData.getFirst("password");
                
                Map<String, Object> result = new HashMap<>();
                
                // 简单模拟登录逻辑
                if ("admin".equals(username) && "password".equals(password)) {
                    result.put("success", true);
                    result.put("message", "登录成功");
                    result.put("token", "valid-token-123456");
                } else {
                    result.put("success", false);
                    result.put("message", "用户名或密码错误");
                }
                
                return Mono.just(result);
            });
    }
} 