package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String origin = headers.getFirst("Origin");
        String host = headers.getFirst("Host");
        String referer = headers.getFirst("Referer");
        
        logger.info("请求路径: {}, 访问时间: {}", 
                exchange.getRequest().getPath(), 
                LocalDateTime.now());
        logger.info("请求来源域: Origin={}, Host={}, Referer={}", 
                origin != null ? origin : "未知", 
                host != null ? host : "未知",
                referer != null ? referer : "未知");
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            logger.info("请求响应状态: {}", 
                    exchange.getResponse().getStatusCode());
        }));
    }

    @Override
    public int getOrder() {
        // 设置过滤器的执行顺序，值越小优先级越高
        return 0;
    }
} 