package com.feeltens.git.mapper;

import com.feeltens.git.entity.SysUserRoleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色Mapper
 *
 * @author feeltens
 * @date 2026-01-11
 */
public interface SysUserRoleMapper {

    /**
     * 根据用户ID查询角色列表
     */
    List<SysUserRoleDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户角色
     */
    int batchInsert(@Param("list") List<SysUserRoleDO> list);

    /**
     * 删除用户的所有角色
     */
    int deleteByUserId(@Param("userId") Long userId);

}