package com.example.gateway.config;

import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import java.time.Duration;

/**
 * Gateway路由配置
 * 
 * 注意：此配置与application.yml中的routes配置共存
 * 两种配置方式会合并，如果存在ID相同的路由，此Java配置会覆盖YAML配置
 */
@Configuration
public class GatewayConfiguration {

    /**
     * 使用Java代码方式配置路由
     * 这些路由与application.yml中定义的路由互补
     * 新增了熔断和限流配置
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // API接口路由 - 以/api开头的路径，添加熔断配置
                .route("api-provider-route", r -> r.path("/api/provider/**")
                        .filters(f -> f
                            .rewritePath("/api/provider/(?<segment>.*)", "/provider/${segment}")
                            // 添加熔断保护，服务不可用时转到回退处理
                            .circuitBreaker(c -> c
                                .setName("providerCircuitBreaker")
                                .setFallbackUri("forward:/fallback/provider")
                                .addStatusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                .addStatusCode(String.valueOf(HttpStatus.BAD_GATEWAY.value()))
                                .addStatusCode(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value())))
                            // 添加重试机制
                            .retry(retryConfig -> retryConfig
                                .setRetries(3)
                                .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, 
                                           HttpStatus.BAD_GATEWAY,
                                           HttpStatus.SERVICE_UNAVAILABLE)
                                .setBackoff(Duration.ofMillis(10), Duration.ofMillis(500), 2, true))
                            )
                        .uri("lb://service-provider"))
                
                // 配置API路由到consumer服务，添加熔断配置
                .route("api-consumer-route", r -> r.path("/api/consumer/**")
                        .filters(f -> f
                            .rewritePath("/api/consumer/(?<segment>.*)", "/consumer/${segment}")
                            // 添加熔断保护，服务不可用时转到回退处理
                            .circuitBreaker(c -> c
                                .setName("consumerCircuitBreaker")
                                .setFallbackUri("forward:/fallback/consumer")
                                .addStatusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                .addStatusCode(String.valueOf(HttpStatus.BAD_GATEWAY.value()))
                                .addStatusCode(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value())))
                            // 添加重试机制
                            .retry(retryConfig -> retryConfig
                                .setRetries(2)
                                .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, 
                                           HttpStatus.BAD_GATEWAY, 
                                           HttpStatus.SERVICE_UNAVAILABLE)
                                .setBackoff(Duration.ofMillis(50), Duration.ofMillis(1000), 2, true))
                            )
                        .uri("lb://service-consumer"))
                        
                // 添加provider的普通路由，确保正常端点也配置熔断
                .route("provider-route-with-circuit-breaker", r -> r.path("/provider/**")
                        .filters(f -> f
                            // 添加熔断保护，服务不可用时转到回退处理
                            .circuitBreaker(c -> c
                                .setName("providerCircuitBreaker")
                                .setFallbackUri("forward:/fallback/provider")
                                .addStatusCode(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                                .addStatusCode(String.valueOf(HttpStatus.BAD_GATEWAY.value()))
                                .addStatusCode(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value())))
                            // 添加重试机制
                            .retry(retryConfig -> retryConfig
                                .setRetries(3)
                                .setStatuses(HttpStatus.INTERNAL_SERVER_ERROR, 
                                           HttpStatus.BAD_GATEWAY,
                                           HttpStatus.SERVICE_UNAVAILABLE)
                                .setBackoff(Duration.ofMillis(10), Duration.ofMillis(500), 2, true))
                            )
                        .uri("lb://service-provider"))
                .build();
    }
} 