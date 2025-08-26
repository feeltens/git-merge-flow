package com.feeltens.git.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * git-merge-flow 配置类
 *
 * @author feeltens
 * @date 2025-08-17
 */
@ConfigurationProperties(prefix = "merge-flow")
@Configuration
@Data
public class GitMergeFlowConfig {

    /**
     * git服务提供方：gitlab、codeup
     * <pre>
     *     GitServiceEnum code
     * </pre>
     */
    private String gitService;

}