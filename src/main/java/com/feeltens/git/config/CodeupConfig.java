package com.feeltens.git.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * codeup 配置类
 *
 * @author feeltens
 * @date 2025-08-17
 */
@ConfigurationProperties(prefix = "codeup.api")
@Configuration
@Data
public class CodeupConfig {

    /**
     * codeup api 基础url
     */
    private String baseUrl;

    /**
     * codeup api 访问令牌
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/obtain-personal-access-token?spm=a2c4g.11186623.help-menu-150040.d_5_0_1.6bf4d4baAOJviI
     *     https://account-devops.aliyun.com/settings/personalAccessToken
     * </pre>
     */
    private String accessToken;

}