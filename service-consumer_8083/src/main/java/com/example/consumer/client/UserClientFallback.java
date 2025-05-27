package com.example.consumer.client;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * UserClient的服务降级实现
 * 用于断路器生效时返回备用响应
 */
@Component("userClientFallbackImpl")
public class UserClientFallback implements UserClient {

    @Override
    public String getUser(int id) {
        return "User服务不可用，无法获取用户信息，这是断路器B的降级响应";
    }

    @Override
    public String createUser(Map<String, String> user) {
        return "User服务不可用，无法创建用户，这是断路器B的降级响应";
    }

    @Override
    public String updateUser(int id, Map<String, String> user) {
        return "User服务不可用，无法更新用户，这是断路器B的降级响应";
    }

    @Override
    public String deleteUser(int id) {
        return "User服务不可用，无法删除用户，这是断路器B的降级响应";
    }
} 