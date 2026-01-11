package com.feeltens.git.config;

import cn.dev33.satoken.stp.StpInterface;
import com.feeltens.git.mapper.SysUserProjectPermMapper;
import com.feeltens.git.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限数据加载实现
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysUserProjectPermMapper sysUserProjectPermMapper;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        // 返回用户有权限的工程ID作为权限码
        List<Long> projectIds = sysUserProjectPermMapper.selectProjectIdsByUserId(userId);
        return projectIds.stream()
                .map(id -> "project:" + id)
                .collect(Collectors.toList());
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(loginId.toString());
        return sysUserRoleMapper.selectRoleCodesByUserId(userId);
    }

}