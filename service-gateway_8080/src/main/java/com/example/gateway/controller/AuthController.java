package com.example.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供简单的登录接口，用于获取认证token
 */
@RestController
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // 模拟用户数据
    private static final Map<String, String> USERS = new HashMap<>();
    
    static {
        USERS.put("admin", "password");
        USERS.put("user", "123456");
    }
    
    /**
     * 登录接口
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含token
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        
        if (USERS.containsKey(username) && USERS.get(username).equals(password)) {
            logger.info("用户登录成功: {}", username);
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("token", "valid-token-123456");
            result.put("username", username);
        } else {
            logger.warn("用户登录失败: {}", username);
            result.put("success", false);
            result.put("message", "用户名或密码错误");
        }
        
        return result;
    }
    
    /**
     * 公开的测试接口，用于测试白名单功能
     * @return 测试信息
     */
    @GetMapping("/public-api")
    public Map<String, Object> publicApi() {
        logger.info("访问公开API");
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "这是一个公开的API，无需认证即可访问");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
} 