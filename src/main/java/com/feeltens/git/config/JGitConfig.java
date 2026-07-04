package com.feeltens.git.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * JGit 冲突解决配置类
 *
 * @author feeltens
 */
@ConfigurationProperties(prefix = "jgit.conflict")
@Configuration
@RefreshScope
@Data
@Slf4j
public class JGitConfig {

    @Resource
    private GitMergeFlowWorkDirConfig workDirConfig;

    /**
     * 获取临时仓库根目录
     * 动态计算：${workDir}/conflict-cache
     */
    public String getTempRepoPath() {
        String workDir = workDirConfig.getWorkDir();
        return workDir + "/conflict-cache";
    }

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

}