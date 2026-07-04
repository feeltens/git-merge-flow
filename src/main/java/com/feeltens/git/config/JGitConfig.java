package com.feeltens.git.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * JGit 冲突解决配置类
 *
 * @author feeltens
 */
@ConfigurationProperties(prefix = "jgit.conflict")
@Configuration
@RefreshScope
@Data
public class JGitConfig {

    /**
     * 临时仓库根目录
     */
    private String tempRepoPath = "/tmp/git-merge-flow";

    /**
     * 会话过期时间（小时）
     */
    private Integer sessionExpireHours = 24;

    /**
     * 克隆超时时间（秒）
     */
    private Integer cloneTimeoutSeconds = 300;

    /**
     * 最大并发会话数（每个工程）
     */
    private Integer maxSessionsPerProject = 10;

    /**
     * 单个冲突文件最大大小（字节），默认 10MB
     * 超过此大小的文件不支持在线解决冲突
     */
    private Long maxConflictFileSize = 10L * 1024 * 1024;

    /**
     * 是否启用稀疏检出优化（API预检+浅克隆+按需检出）
     * 默认启用，可通过配置关闭
     */
    private boolean enableSparseCheckout = true;

    /**
     * 是否启用浅克隆优化
     * 浅克隆只下载最新提交，大幅减少下载量和克隆时间
     * 默认启用，可通过配置关闭
     */
    private boolean enableShallowClone = true;

    /**
     * 浅克隆深度
     * 默认为1，表示只下载最新提交
     */
    private Integer shallowCloneDepth = 1;

}