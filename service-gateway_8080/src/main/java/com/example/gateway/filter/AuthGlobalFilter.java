package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 全局权限认证过滤器
 * 检查请求头中是否包含token，并验证token的有效性
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthGlobalFilter.class);
    
    // 无需认证的路径
    private static final List<String> WHITELIST = Arrays.asList(
            "/actuator",           // 监控端点
            "/cors-test/login",    // CORS测试登录接口
            "/cors-test/public-api", // CORS测试公开接口
            "/lb-test",            // 负载均衡测试路径
            "/cors-test.html",     // CORS测试页面
            "/favicon.ico",        // 网站图标
            "/static/",            // 静态资源
            "/webjars/"           // WebJars资源
    );
    
    // 模拟的有效token
    private static final String VALID_TOKEN = "valid-token-123456";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查是否是白名单路径
        if (isWhiteListPath(path)) {
            logger.info("白名单路径，无需认证: {}", path);
            return chain.filter(exchange);
        }
        
        // 从请求头获取token
        String token = request.getHeaders().getFirst("X-Auth-Token");
        
        // 验证token
        if (token != null && token.equals(VALID_TOKEN)) {
            logger.info("认证成功: {}", path);
            return chain.filter(exchange);
        } else {
            logger.warn("认证失败，拒绝访问: {} token: {}", path, token);
            return unauthorizedResponse(exchange);
        }
    }
    
    /**
     * 判断当前路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 返回未授权的响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        
        String message = "未授权，请提供有效的认证token";
        DataBuffer buffer = response.bufferFactory().wrap(message.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 确保认证过滤器优先级高于LoggingGlobalFilter
        return -1;
    }
} 