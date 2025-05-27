package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局限流过滤器
 * 基于IP地址和路径的请求限制
 */
@Component
public class RateLimiterGlobalFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterGlobalFilter.class);
    
    // 使用内存方案实现简单限流（实际生产中应使用Redis分布式限流）
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    // 不进行限流的白名单路径
    private static final List<String> WHITELIST = Arrays.asList(
            "/fallback",        // 回退路径
            "/actuator",        // 监控端点
            "/cors-test.html",  // CORS测试页面
            "/favicon.ico",     // 网站图标
            "/static/",         // 静态资源
            "/webjars/"         // WebJars资源
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查白名单
        if (isWhiteListPath(path)) {
            return chain.filter(exchange);
        }
        
        // 获取客户端IP
        String clientIP = getClientIP(request);
        
        // 构造限流KEY
        String serviceName = getServiceFromPath(path);
        String key = clientIP + ":" + serviceName;
        
        // 获取或创建令牌桶
        TokenBucket bucket = buckets.computeIfAbsent(key, k -> createBucket(serviceName));
        
        // 尝试获取令牌
        if (bucket.tryAcquire()) {
            return chain.filter(exchange);
        } else {
            logger.warn("请求被限流: IP={}, 路径={}, 服务={}", clientIP, path, serviceName);
            return createRateLimitResponse(exchange);
        }
    }
    
    /**
     * 创建对应服务的令牌桶
     */
    private TokenBucket createBucket(String serviceName) {
        // 根据不同的服务配置不同的限流规则
        if ("service-provider".equals(serviceName)) {
            // Provider服务：每秒最多50个请求，突发最多60个请求
            return new TokenBucket(50, 60);
        } else if ("service-consumer".equals(serviceName)) {
            // Consumer服务：每秒最多30个请求，突发最多40个请求
            return new TokenBucket(30, 40);
        } else {
            // 默认规则：每秒最多20个请求，突发最多30个请求
            return new TokenBucket(20, 30);
        }
    }
    
    /**
     * 从路径中提取服务名称
     */
    private String getServiceFromPath(String path) {
        if (path.startsWith("/provider") || path.contains("/lb-test")) {
            return "service-provider";
        } else if (path.startsWith("/consumer")) {
            return "service-consumer";
        } else {
            return "default";
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIP(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ? 
                    request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 判断当前路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 创建限流响应
     */
    private Mono<Void> createRateLimitResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS); // 429状态码
        
        String message = "请求过于频繁，请稍后再试";
        DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 限流过滤器优先级高于认证过滤器
        return -10;
    }
    
    /**
     * 令牌桶限流算法的简单实现
     */
    private static class TokenBucket {
        // 每秒生成的令牌数量
        private final double tokensPerSecond;
        // 令牌桶容量
        private final double capacity;
        // 当前令牌数量
        private double availableTokens;
        // 上次更新时间
        private long lastRefillTime;
        
        public TokenBucket(double tokensPerSecond, double capacity) {
            this.tokensPerSecond = tokensPerSecond;
            this.capacity = capacity;
            this.availableTokens = capacity;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        /**
         * 尝试获取一个令牌
         * @return 是否获取成功
         */
        public synchronized boolean tryAcquire() {
            refill();
            
            if (availableTokens >= 1) {
                availableTokens -= 1;
                return true;
            } else {
                return false;
            }
        }
        
        /**
         * 补充令牌
         */
        private void refill() {
            long now = System.currentTimeMillis();
            double secondsSinceLastRefill = (now - lastRefillTime) / 1000.0;
            double tokensToAdd = secondsSinceLastRefill * tokensPerSecond;
            
            if (tokensToAdd > 0) {
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
} 