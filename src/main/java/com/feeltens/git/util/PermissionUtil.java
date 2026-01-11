package com.feeltens.git.util;

import cn.dev33.satoken.stp.StpUtil;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.entity.GitBranchDO;
import com.feeltens.git.entity.GitMixBranchDO;
import com.feeltens.git.mapper.GitBranchMapper;
import com.feeltens.git.mapper.GitMixBranchMapper;
import com.feeltens.git.mapper.SysUserProjectPermMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 权限校验工具类
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Component
public class PermissionUtil {

    @Resource
    private SysUserProjectPermMapper permMapper;

    @Resource
    private GitBranchMapper branchMapper;

    @Resource
    private GitMixBranchMapper mixBranchMapper;

    /**
     * 校验当前用户是否有项目权限
     * 管理员默认有所有权限
     *
     * @param projectId 项目ID
     */
    public void checkProjectPermission(Long projectId) {
        if (projectId == null) {
            return;
        }

        // 管理员跳过校验
        if (StpUtil.hasRole("ADMIN")) {
            return;
        }

        Long userId = StpUtil.getLoginIdAsLong();
        boolean hasPerm = permMapper.existsByUserIdAndProjectId(userId, projectId);
        if (!hasPerm) {
            throw new BizException("无该项目操作权限");
        }
    }

    /**
     * 通过分支ID校验项目权限
     *
     * @param branchId 分支ID
     */
    public void checkPermissionByBranchId(Long branchId) {
        if (branchId == null) {
            return;
        }

        GitBranchDO branch = branchMapper.queryByBranchId(branchId);
        if (branch != null) {
            checkProjectPermission(branch.getProjectId());
        }
    }

    /**
     * 通过中间分支ID校验项目权限
     *
     * @param mixBranchId 中间分支ID
     */
    public void checkPermissionByMixBranchId(Long mixBranchId) {
        if (mixBranchId == null) {
            return;
        }

        GitMixBranchDO mixBranch = mixBranchMapper.queryByMixBranchId(mixBranchId);
        if (mixBranch != null) {
            checkProjectPermission(mixBranch.getProjectId());
        }
    }

    /**
     * 检查当前用户是否有项目权限（不抛异常）
     *
     * @param projectId 项目ID
     * @return 是否有权限
     */
    public boolean hasProjectPermission(Long projectId) {
        if (projectId == null) {
            return false;
        }

        if (StpUtil.hasRole("ADMIN")) {
            return true;
        }

        Long userId = StpUtil.getLoginIdAsLong();
        return permMapper.existsByUserIdAndProjectId(userId, projectId);
    }

}