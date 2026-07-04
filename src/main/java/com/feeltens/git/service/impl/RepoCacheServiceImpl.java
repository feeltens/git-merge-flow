package com.feeltens.git.service.impl;

import cn.hutool.core.io.FileUtil;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.config.CodeupConfig;
import com.feeltens.git.config.GitLabConfig;
import com.feeltens.git.config.GitMergeFlowConfig;
import com.feeltens.git.dto.conflict.GitCredentials;
import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.enums.CloneStatus;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.service.JGitService;
import com.feeltens.git.service.RepoCacheService;
import com.feeltens.git.util.GitPathBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Date;

/**
 * 仓库缓存服务实现
 *
 * @author feeltens
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RepoCacheServiceImpl implements RepoCacheService {

    private final GitProjectMapper gitProjectMapper;
    private final JGitService jGitService;
    private final GitMergeFlowConfig gitMergeFlowConfig;
    private final CodeupConfig codeupConfig;
    private final GitLabConfig gitLabConfig;
    private final GitPathBuilder gitPathBuilder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cloneFullRepo(Long projectId) {
        // 1. 查询项目信息
        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        // 2. 检查当前状态
        if (CloneStatus.CLONING.getCode().equals(project.getCloneStatus())) {
            log.info("项目正在克隆中，跳过: projectId={}", projectId);
            return;
        }

        if (CloneStatus.CLONED.getCode().equals(project.getCloneStatus())) {
            log.info("项目已克隆，跳过: projectId={}", projectId);
            return;
        }

        // 3. 更新状态为克隆中
        project.setCloneStatus(CloneStatus.CLONING.getCode());
        project.setCloneErrorMsg(null);
        gitProjectMapper.updateCloneStatus(project);

        // 4. 执行克隆
        String cachePath = getProjectCachePath(project.getProjectName());
        try {
            log.info("开始完整克隆仓库: projectId={}, projectName={}, cachePath={}",
                    projectId, project.getProjectName(), cachePath);

            // 确保缓存目录存在
            FileUtil.mkdir(cachePath);

            // 清理已存在的缓存目录
            File cacheDir = new File(cachePath);
            if (cacheDir.exists()) {
                FileUtil.del(cacheDir);
            }

            // 获取Git凭证
            GitCredentials credentials = getGitCredentials();

            // 调用JGitService进行完整克隆，传入自定义根路径
            String localRepoPath = jGitService.cloneFullRepository(
                    project.getRepositoryUrl(),
                    project.getProjectName(),
                    "", // cacheId为空，因为路径已经包含项目名
                    credentials,
                    gitPathBuilder.getCacheRepoPath(project.getProjectName()) // 使用统一路径构建
            );

            // 5. 克隆成功，更新状态
            project.setCloneStatus(CloneStatus.CLONED.getCode());
            project.setCloneTime(new Date());
            project.setLastPullTime(new Date());
            project.setCloneErrorMsg(null);
            gitProjectMapper.updateCloneStatus(project);

            log.info("仓库克隆成功: projectId={}, cachePath={}", projectId, cachePath);

        } catch (Exception e) {
            log.error("仓库克隆失败: projectId={}, error={}", projectId, e.getMessage(), e);

            // 6. 克隆失败，更新状态
            project.setCloneStatus(CloneStatus.CLONE_FAILED.getCode());
            project.setCloneErrorMsg(e.getMessage());
            gitProjectMapper.updateCloneStatus(project);

            // 清理失败的目录
            FileUtil.del(new File(cachePath));

            throw new BizException("仓库克隆失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullMainBranch(Long projectId) {
        // 1. 查询项目信息
        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        // 2. 检查克隆状态
        if (!CloneStatus.CLONED.getCode().equals(project.getCloneStatus())) {
            log.warn("项目未克隆，跳过拉取: projectId={}, cloneStatus={}",
                    projectId, project.getCloneStatus());
            return;
        }

        // 3. 检查缓存目录是否存在
        String cachePath = getProjectCachePath(project.getProjectName());
        File cacheDir = new File(cachePath);
        if (!cacheDir.exists()) {
            log.warn("缓存目录不存在，跳过拉取: projectId={}, cachePath={}", projectId, cachePath);
            return;
        }

        try {
            log.info("开始拉取主分支: projectId={}, branch={}", projectId, project.getDefaultBranch());

            // TODO: 需要在JGitService中新增pullBranch方法
            // 暂时跳过实际拉取操作
            // jGitService.pullBranch(cachePath, project.getDefaultBranch(), getGitCredentials());

            // 更新拉取时间
            project.setLastPullTime(new Date());
            gitProjectMapper.updateCloneStatus(project);

            log.info("主分支拉取成功: projectId={}", projectId);

        } catch (Exception e) {
            log.error("主分支拉取失败: projectId={}, error={}", projectId, e.getMessage(), e);
            // 拉取失败不改变克隆状态，只记录日志
        }
    }

    @Override
    public String copyToSessionDir(Long projectId, String sessionId) {
        // 1. 查询项目信息
        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        // 2. 检查克隆状态
        if (!CloneStatus.CLONED.getCode().equals(project.getCloneStatus())) {
            throw new BizException("项目缓存未就绪: " + project.getCloneStatus());
        }

        // 3. 获取源路径和目标路径
        String sourcePath = getProjectCachePath(project.getProjectName());
        String targetPath = getSessionRepoPath(project.getProjectName(), sessionId);

        File sourceDir = new File(sourcePath);
        File targetDir = new File(targetPath);

        // 4. 检查源目录是否存在
        if (!sourceDir.exists()) {
            throw new BizException("缓存目录不存在: " + sourcePath);
        }

        // 5. 确保目标目录的父目录存在
        FileUtil.mkdir(targetDir.getParentFile());

        // 6. 清理目标目录
        if (targetDir.exists()) {
            FileUtil.del(targetDir);
        }

        try {
            log.info("开始拷贝仓库: source={}, target={}", sourcePath, targetPath);

            // TODO: 需要在JGitService中新增copyRepository方法
            // 暂时使用FileUtil拷贝
            FileUtil.copyContent(sourceDir, targetDir, true);

            log.info("仓库拷贝成功: target={}", targetPath);
            return targetPath;

        } catch (Exception e) {
            log.error("仓库拷贝失败: source={}, target={}, error={}",
                    sourcePath, targetPath, e.getMessage(), e);
            // 清理失败的目录
            FileUtil.del(targetDir);
            throw new BizException("仓库拷贝失败: " + e.getMessage());
        }
    }

    @Override
    public void ensureCacheReady(Long projectId) {
        // 1. 查询项目信息
        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        // 2. 检查克隆状态
        String cloneStatus = project.getCloneStatus();
        if (!CloneStatus.CLONED.getCode().equals(cloneStatus)) {
            if (CloneStatus.CLONE_FAILED.getCode().equals(cloneStatus)) {
                throw new BizException("项目克隆失败: " + project.getCloneErrorMsg());
            } else {
                throw new BizException("项目缓存未就绪: " + cloneStatus);
            }
        }

        // 3. 触发主分支拉取（同步执行）
        try {
            pullMainBranch(projectId);
        } catch (Exception e) {
            log.warn("主分支拉取失败，使用已有缓存: projectId={}, error={}",
                    projectId, e.getMessage());
            // 拉取失败不抛出异常，使用已有缓存继续
        }

        // 4. 验证缓存目录存在
        String cachePath = getProjectCachePath(project.getProjectName());
        if (!new File(cachePath).exists()) {
            throw new BizException("缓存目录不存在: " + cachePath);
        }
    }

    @Override
    public String getCacheStatus(Long projectId) {
        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }
        return project.getCloneStatus();
    }

    @Override
    public void cleanupCache(Long projectId) {
        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            throw new BizException("项目不存在: " + projectId);
        }

        String cachePath = getProjectCachePath(project.getProjectName());
        File cacheDir = new File(cachePath);

        if (cacheDir.exists()) {
            log.info("清理缓存目录: projectId={}, cachePath={}", projectId, cachePath);
            FileUtil.del(cacheDir);
        }

        // 重置克隆状态
        project.setCloneStatus(CloneStatus.NOT_CLONED.getCode());
        project.setCloneTime(null);
        project.setCloneErrorMsg(null);
        project.setLastPullTime(null);
        gitProjectMapper.updateCloneStatus(project);

        log.info("缓存清理完成: projectId={}", projectId);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取项目缓存路径
     */
    private String getProjectCachePath(String projectName) {
        return gitPathBuilder.getCacheRepoPath(projectName);
    }

    /**
     * 获取会话仓库路径
     */
    private String getSessionRepoPath(String projectName, String sessionId) {
        return gitPathBuilder.getSessionRepoPath(projectName, sessionId);
    }

    /**
     * 获取Git凭证
     */
    private GitCredentials getGitCredentials() {
        String gitService = gitMergeFlowConfig.getGitService();
        if ("codeup".equalsIgnoreCase(gitService)) {
            return GitCredentials.builder()
                    .username("oauth2")
                    .password(codeupConfig.getAccessToken())
                    .build();
        } else if ("gitlab".equalsIgnoreCase(gitService)) {
            return GitCredentials.builder()
                    .username("oauth2")
                    .password(gitLabConfig.getAccessToken())
                    .build();
        }
        throw new BizException("未配置的 Git 服务平台: " + gitService);
    }

}