package com.feeltens.git.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 仓库缓存配置类
 *
 * @author feeltens
 */
@ConfigurationProperties(prefix = "merge-flow.repo-cache")
@Configuration
@RefreshScope
@Data
@Slf4j
public class RepoCacheConfig {

    @Resource
    private GitMergeFlowWorkDirConfig workDirConfig;

    /**
     * 是否启用仓库缓存
     */
    private boolean enabled = true;

    /**
     * 获取缓存根目录
     * 动态计算：${workDir}/repo-cache
     */
    public String getCachePath() {
        String workDir = workDirConfig.getWorkDir();
        return workDir + "/repo-cache";
    }

    /**
     * 最大缓存大小（GB）
     */
    private Long maxCacheSizeGb = 100L;

    /**
     * 克隆超时时间（秒）
     */
    private Integer cloneTimeoutSeconds = 600;

    /**
     * 拉取超时时间（秒）
     */
    private Integer pullTimeoutSeconds = 300;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 重试间隔（秒）
     */
    private Integer retryIntervalSeconds = 60;

}