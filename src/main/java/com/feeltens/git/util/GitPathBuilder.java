package com.feeltens.git.util;

import com.feeltens.git.config.JGitConfig;
import com.feeltens.git.config.RepoCacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * Git路径构建工具类
 * 统一管理所有Git仓库路径的构建逻辑
 *
 * @author feeltens
 */
@Component
@Slf4j
public class GitPathBuilder {

    @Resource
    private JGitConfig jgitConfig;
    @Resource
    private RepoCacheConfig repoCacheConfig;

    /**
     * 获取缓存仓库路径
     * 格式：{cachePath}/{projectName}
     *
     * @param projectName 项目名称
     * @return 缓存仓库路径
     */
    public String getCacheRepoPath(String projectName) {
        return repoCacheConfig.getCachePath() + File.separator + projectName;
    }

    /**
     * 获取会话仓库路径
     * 格式：{tempRepoPath}/{projectName}/{sessionId}
     *
     * @param projectName 项目名称
     * @param sessionId   会话ID
     * @return 会话仓库路径
     */
    public String getSessionRepoPath(String projectName, String sessionId) {
        return jgitConfig.getTempRepoPath() + File.separator + projectName + File.separator + sessionId;
    }

    /**
     * 获取项目路径（不包含sessionId）
     * 格式：{tempRepoPath}/{projectName}
     *
     * @param projectName 项目名称
     * @return 项目路径
     */
    public String getProjectPath(String projectName) {
        return jgitConfig.getTempRepoPath() + File.separator + projectName;
    }

}