package com.example.consumer.controller;

import com.example.consumer.client.UserClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/consumer/users")
public class UserConsumerController {

    @Autowired
    private UserClient userClient;  // 移除Qualifier注解，让Spring自动注入

    @GetMapping("/{id}")
    @CircuitBreaker(name = "circuitBreakerB", fallbackMethod = "getUserFallback")
    public ResponseEntity<String> getUser(@PathVariable int id) {
        return ResponseEntity.ok(userClient.getUser(id));
    }

    @PostMapping
    @CircuitBreaker(name = "circuitBreakerB", fallbackMethod = "createUserFallback")
    public ResponseEntity<String> createUser(@RequestBody Map<String, String> user) {
        return ResponseEntity.ok(userClient.createUser(user));
    }

    @PutMapping("/{id}")
    @CircuitBreaker(name = "circuitBreakerB", fallbackMethod = "updateUserFallback")
    public ResponseEntity<String> updateUser(@PathVariable int id, @RequestBody Map<String, String> user) {
        return ResponseEntity.ok(userClient.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "circuitBreakerB", fallbackMethod = "deleteUserFallback")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        return ResponseEntity.ok(userClient.deleteUser(id));
    }
    
    /**
     * 断路器B的本地降级方法
     */
    public ResponseEntity<String> getUserFallback(int id, Exception ex) {
        return ResponseEntity.ok("获取用户信息失败，这是本地断路器B的降级响应: " + ex.getMessage());
    }
    
    public ResponseEntity<String> createUserFallback(Map<String, String> user, Exception ex) {
        return ResponseEntity.ok("创建用户失败，这是本地断路器B的降级响应: " + ex.getMessage());
    }
    
    public ResponseEntity<String> updateUserFallback(int id, Map<String, String> user, Exception ex) {
        return ResponseEntity.ok("更新用户失败，这是本地断路器B的降级响应: " + ex.getMessage());
    }
    
    public ResponseEntity<String> deleteUserFallback(int id, Exception ex) {
        return ResponseEntity.ok("删除用户失败，这是本地断路器B的降级响应: " + ex.getMessage());
    }
}
