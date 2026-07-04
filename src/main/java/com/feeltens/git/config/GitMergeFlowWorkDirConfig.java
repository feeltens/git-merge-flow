package com.feeltens.git.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Git Merge Flow 工作目录配置
 *
 * @author feeltens
 */
@Component
@ConfigurationProperties(prefix = "merge-flow")
@Data
public class GitMergeFlowWorkDirConfig {

    /**
     * 工作根目录
     * 默认: /tmp/git-merge-flow/workspace
     */
    private String workDir = "/tmp/git-merge-flow/workspace";

}