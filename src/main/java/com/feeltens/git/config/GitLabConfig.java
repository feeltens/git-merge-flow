package com.feeltens.git.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * gitlab 配置类
 *
 * @author feeltens
 * @date 2025-08-17
 */
@ConfigurationProperties(prefix = "gitlab.api")
@Configuration
@Data
public class GitLabConfig {

    /**
     * api 基础url
     */
    private String baseUrl;

    /**
     * api 访问令牌
     * <pre>
     *     https://docs.gitlab.com/18.3/api/rest/authentication/
     *     用户设置 -> 访问令牌 -> 个人访问令牌
     * </pre>
     */
    private String accessToken;

}