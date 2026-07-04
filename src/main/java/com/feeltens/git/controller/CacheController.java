package com.feeltens.git.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.enums.CloneStatus;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.mapper.SysUserProjectPermMapper;
import com.feeltens.git.service.RepoCacheService;
import com.feeltens.git.vo.base.CloudResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库缓存监控接口
 *
 * @author feeltens
 */
@RestController
@RequestMapping("/api/v1/cache")
@Slf4j
@RequiredArgsConstructor
public class CacheController {

    private final RepoCacheService repoCacheService;
    private final GitProjectMapper gitProjectMapper;
    private final SysUserProjectPermMapper sysUserProjectPermMapper;

    /**
     * 查询缓存状态
     */
    @GetMapping("/status/{projectId}")
    public CloudResponse<Map<String, Object>> getCacheStatus(@PathVariable Long projectId) {
        // 权限检查
        checkPermission(projectId);

        GitProjectDO project = gitProjectMapper.queryByProjectId(projectId);
        if (project == null) {
            return CloudResponse.fail("项目不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("projectId", project.getProjectId());
        result.put("projectName", project.getProjectName());
        result.put("cloneStatus", project.getCloneStatus());
        result.put("cloneTime", project.getCloneTime());
        result.put("cloneErrorMsg", project.getCloneErrorMsg());
        result.put("lastPullTime", project.getLastPullTime());

        // 判断缓存目录是否存在
        String cachePath = "/Volumes/idata/dev/appdata/log/git-merge-flow/repo-cache/" + project.getProjectName();
        File cacheDir = new File(cachePath);
        result.put("cacheExists", cacheDir.exists());
        if (cacheDir.exists()) {
            result.put("cacheSize", calculateDirSize(cacheDir));
        }

        return CloudResponse.success(result);
    }

    /**
     * 查询缓存统计
     */
    @GetMapping("/statistics")
    public CloudResponse<Map<String, Object>> getCacheStatistics() {
        List<GitProjectDO> allProjects = gitProjectMapper.selectAll();

        int totalCount = allProjects.size();
        int notClonedCount = 0;
        int cloningCount = 0;
        int clonedCount = 0;
        int cloneFailedCount = 0;
        long totalCacheSize = 0;

        for (GitProjectDO project : allProjects) {
            String status = project.getCloneStatus();
            if (CloneStatus.NOT_CLONED.getCode().equals(status)) {
                notClonedCount++;
            } else if (CloneStatus.CLONING.getCode().equals(status)) {
                cloningCount++;
            } else if (CloneStatus.CLONED.getCode().equals(status)) {
                clonedCount++;
                // 计算缓存大小
                String cachePath = "/Volumes/idata/dev/appdata/log/git-merge-flow/repo-cache/" + project.getProjectName();
                File cacheDir = new File(cachePath);
                if (cacheDir.exists()) {
                    totalCacheSize += calculateDirSize(cacheDir);
                }
            } else if (CloneStatus.CLONE_FAILED.getCode().equals(status)) {
                cloneFailedCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalProjects", totalCount);
        result.put("notCloned", notClonedCount);
        result.put("cloning", cloningCount);
        result.put("cloned", clonedCount);
        result.put("cloneFailed", cloneFailedCount);
        result.put("totalCacheSize", totalCacheSize);
        result.put("totalCacheSizeMB", totalCacheSize / (1024 * 1024));

        return CloudResponse.success(result);
    }

    /**
     * 手动刷新缓存
     */
    @PostMapping("/refresh/{projectId}")
    public CloudResponse<String> refreshCache(@PathVariable Long projectId) {
        // 权限检查
        checkPermission(projectId);

        try {
            repoCacheService.pullMainBranch(projectId);
            return CloudResponse.success("缓存刷新成功");
        } catch (Exception e) {
            log.error("缓存刷新失败: projectId={}, error={}", projectId, e.getMessage(), e);
            return CloudResponse.fail("缓存刷新失败: " + e.getMessage());
        }
    }

    /**
     * 清理缓存
     */
    @PostMapping("/cleanup/{projectId}")
    public CloudResponse<String> cleanupCache(@PathVariable Long projectId) {
        // 权限检查
        checkPermission(projectId);

        try {
            repoCacheService.cleanupCache(projectId);
            return CloudResponse.success("缓存清理成功");
        } catch (Exception e) {
            log.error("缓存清理失败: projectId={}, error={}", projectId, e.getMessage(), e);
            return CloudResponse.fail("缓存清理失败: " + e.getMessage());
        }
    }

    /**
     * 权限检查
     */
    private void checkPermission(Long projectId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean hasPermission = sysUserProjectPermMapper.existsByUserIdAndProjectId(userId, projectId);
        if (!hasPermission) {
            throw new BizException("无权限访问该项目");
        }
    }

    /**
     * 计算目录大小
     */
    private long calculateDirSize(File dir) {
        long size = 0;
        if (dir.isFile()) {
            return dir.length();
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += calculateDirSize(file);
            }
        }
        return size;
    }

}