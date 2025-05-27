package com.example.provider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);  // 定义日志

    private final Map<Integer, String> userStore = new ConcurrentHashMap<>();

    // 1. 获取用户信息
    @GetMapping("/{id}")
    public ResponseEntity<String> getUser(@PathVariable Integer id) {
        try {
            Thread.sleep(3000); // 添加3秒延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("收到获取用户请求，用户ID: {}", id);  // 日志记录
        if (userStore.containsKey(id)) {
            logger.info("用户存在，返回用户信息: {}", userStore.get(id));
            return ResponseEntity.ok(userStore.get(id));
        } else {
            logger.warn("用户不存在，ID: {}", id);
            return ResponseEntity.status(404).body("用户不存在");
        }
    }

    // 2. 创建用户
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody Map<String, String> user) {
        Integer id = Integer.parseInt(user.get("id"));
        String name = user.get("name");
        logger.info("收到创建用户请求，用户ID: {}, 用户名: {}", id, name);
        userStore.put(id, name);
        logger.info("用户创建成功: {}", name);
        return ResponseEntity.ok("创建成功: " + name);
    }

    // 3. 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @RequestBody Map<String, String> user) {
        logger.info("收到更新用户请求，用户ID: {}, 新用户名: {}", id, user.get("name"));
        if (!userStore.containsKey(id)) {
            logger.warn("更新失败，用户ID: {} 不存在", id);
            return ResponseEntity.status(404).body("用户不存在");
        }
        userStore.put(id, user.get("name"));
        logger.info("用户更新成功: {}", user.get("name"));
        return ResponseEntity.ok("更新成功: " + user.get("name"));
    }

    // 4. 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Integer id) {
        logger.info("收到删除用户请求，用户ID: {}", id);
        if (!userStore.containsKey(id)) {
            logger.warn("删除失败，用户ID: {} 不存在", id);
            return ResponseEntity.status(404).body("用户不存在");
        }
        userStore.remove(id);
        logger.info("用户删除成功，ID: {}", id);
        return ResponseEntity.ok("删除成功");
    }
}
