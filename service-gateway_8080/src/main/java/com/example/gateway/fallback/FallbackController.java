package com.example.gateway.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 熔断回退控制器
 * 当服务调用被熔断或超时时，提供默认的回退响应
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    /**
     * 默认的回退处理方法
     * @param exchange 服务器Web交换对象
     * @return 包含错误信息的响应
     */
    @GetMapping("/default")
    public Mono<Map<String, Object>> defaultFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        String serviceId = getServiceIdFromPath(path);
        
        logger.warn("服务调用失败，执行熔断回退处理: 路径={}, 服务={}", path, serviceId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "服务暂时不可用，请稍后再试");
        result.put("code", "SERVICE_UNAVAILABLE");
        result.put("path", path);
        result.put("timestamp", System.currentTimeMillis());
        
        if (serviceId != null) {
            result.put("service", serviceId);
        }
        
        return Mono.just(result);
    }
    
    /**
     * Provider服务的回退处理
     */
    @GetMapping("/provider")
    public Mono<Map<String, Object>> providerFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        logger.warn("Provider服务调用失败，执行熔断回退: 路径={}", path);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Provider服务暂时不可用，请稍后再试");
        result.put("code", "PROVIDER_UNAVAILABLE");
        result.put("path", path);
        result.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(result);
    }
    
    /**
     * Consumer服务的回退处理
     */
    @GetMapping("/consumer")
    public Mono<Map<String, Object>> consumerFallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        logger.warn("Consumer服务调用失败，执行熔断回退: 路径={}", path);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Consumer服务暂时不可用，请稍后再试");
        result.put("code", "CONSUMER_UNAVAILABLE");
        result.put("path", path);
        result.put("timestamp", System.currentTimeMillis());
        
        return Mono.just(result);
    }
    
    /**
     * 从请求路径中提取服务ID
     */
    private String getServiceIdFromPath(String path) {
        if (path.contains("/provider/")) {
            return "service-provider";
        } else if (path.contains("/consumer/")) {
            return "service-consumer";
        }
        return null;
    }
} 