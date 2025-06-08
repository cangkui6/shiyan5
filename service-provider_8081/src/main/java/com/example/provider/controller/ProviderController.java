package com.example.provider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RefreshScope  // 添加RefreshScope注解，支持动态配置刷新
@RestController
@RequestMapping("/provider")
public class ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderController.class);
    
    @Value("${server.port}")
    private String serverPort;
    
    // 添加可动态刷新的配置属性
    @Value("${provider.message:默认配置消息}")
    private String configMessage;

    @GetMapping("/hello")
    public String hello() {
        logger.info("收到 /provider/hello 请求，当前实例端口：{}", serverPort);
        return "Hello from Service Provider，实例端口：" + serverPort;
    }
    
    @GetMapping("/heavy-task/{taskId}")
    public String performHeavyTask(@PathVariable String taskId) {
        logger.info("收到繁重任务请求，taskId: {}，当前实例端口：{}", taskId, serverPort);
        try {
            // 模拟耗时操作
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return String.format("任务 %s 已在Service Provider实例（端口：%s）处理完成", taskId, serverPort);
    }
    
    @GetMapping("/lb-test")
    public String loadBalanceTest() {
        logger.info("收到负载均衡测试请求，当前实例端口：{}", serverPort);
        return "负载均衡测试响应，来自Provider实例，端口：" + serverPort;
    }
    
    /**
     * 错误测试端点 - 用于测试熔断机制
     * 始终返回500错误
     */
    @GetMapping("/error")
    public String error() {
        logger.info("收到 /provider/error 请求，返回异常，当前实例端口：{}", serverPort);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "模拟服务错误，用于测试熔断");
    }
    
    /**
     * 用于测试配置动态刷新的端点
     * 返回从配置中心获取的消息
     */
    @GetMapping("/config-message")
    public String getConfigMessage() {
        logger.info("获取配置消息，当前消息：{}", configMessage);
        return "配置中心的消息：" + configMessage;
    }
}
