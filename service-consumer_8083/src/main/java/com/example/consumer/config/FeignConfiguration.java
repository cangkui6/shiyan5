package com.example.consumer.config;

import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端配置类
 * 启用Feign客户端的断路器支持
 */
@Configuration
public class FeignConfiguration {
    
    /**
     * 自定义错误解码器，处理Feign调用异常
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }
    
    /**
     * 确保客户端配置生效
     */
    @Bean
    public FeignClientFactoryCustomizer feignClientFactoryCustomizer() {
        return feignClientFactory -> {
            feignClientFactory.setConnectTimeout(5000);
            feignClientFactory.setReadTimeout(5000);
        };
    }
    
    /**
     * 自定义客户端工厂配置类
     */
    public interface FeignClientFactoryCustomizer {
        void customize(FeignClientFactory factory);
    }
    
    /**
     * Feign客户端工厂
     */
    public static class FeignClientFactory {
        private int connectTimeout = 5000;
        private int readTimeout = 5000;
        
        public int getConnectTimeout() {
            return connectTimeout;
        }
        
        public void setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
        }
        
        public int getReadTimeout() {
            return readTimeout;
        }
        
        public void setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
        }
    }
} 