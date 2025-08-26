package com.feeltens.git.config;

import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
        return configuration -> {
            // 创建拦截器实例
            SqlPerformanceInterceptor interceptor = new SqlPerformanceInterceptor();
            // 添加拦截器
            configuration.addInterceptor(interceptor);
        };
    }

}