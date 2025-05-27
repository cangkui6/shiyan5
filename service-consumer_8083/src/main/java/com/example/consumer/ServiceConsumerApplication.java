package com.example.consumer;

import com.example.consumer.config.FeignConfiguration;
import com.example.consumer.config.LoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
@EnableDiscoveryClient  // 启用 Eureka 客户端
@EnableFeignClients(basePackages = "com.example.consumer.client", defaultConfiguration = FeignConfiguration.class)  // 明确指定扫描包路径和默认配置
@LoadBalancerClient(name = "SERVICE-PROVIDER", configuration = LoadBalancerConfig.class)
@ComponentScan(basePackages = {"com.example.consumer"})  // 确保组件被扫描到
public class ServiceConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceConsumerApplication.class, args);
    }

    @Bean
    @LoadBalanced  // 启用负载均衡支持，自动使用服务发现机制
    public RestTemplate restTemplate() {
        // 使用RestTemplateBuilder设置超时时间
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(5000))
                .setReadTimeout(Duration.ofMillis(5000))
                .build();
    }
}
