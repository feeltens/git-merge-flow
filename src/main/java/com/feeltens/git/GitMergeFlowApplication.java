package com.feeltens.git;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 合流管理系统 启动类
 *
 * @author feeltens
 */
@SpringBootApplication
@MapperScan("com.feeltens.git.mapper")
@Slf4j
public class GitMergeFlowApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(GitMergeFlowApplication.class, args);
        log.info("=================================================");
        log.info("===\u001B[33m" + "   GitMergeFlowApplication start success   " + "\u001B[0m===");
        log.info("=================================================");
        log.info("测试地址: \thttp://127.0.0.1:{}/", context.getEnvironment().getProperty("server.port"));
    }

}