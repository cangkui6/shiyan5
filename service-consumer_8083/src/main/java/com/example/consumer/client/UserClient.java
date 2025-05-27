package com.example.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
    name = "service-provider", 
    contextId = "userClient", 
    fallback = UserClientFallback.class,
    url = "${provider.service.url:http://localhost:8081}"  // 提供默认URL，避免服务发现失败
)
@Primary
public interface UserClient {

    @GetMapping("/users/{id}")
    String getUser(@PathVariable("id") int id);

    @PostMapping("/users")
    String createUser(@RequestBody Map<String, String> user);

    @PutMapping("/users/{id}")
    String updateUser(@PathVariable("id") int id, @RequestBody Map<String, String> user);

    @DeleteMapping("/users/{id}")
    String deleteUser(@PathVariable("id") int id);
}
