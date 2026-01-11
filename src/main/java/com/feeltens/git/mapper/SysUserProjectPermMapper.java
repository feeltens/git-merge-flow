package com.feeltens.git.mapper;

import com.feeltens.git.entity.SysUserProjectPermDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Project权限Mapper
 *
 * @author feeltens
 * @date 2026-01-11
 */
public interface SysUserProjectPermMapper {

    /**
     * 根据用户ID查询权限列表
     */
    List<SysUserProjectPermDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询有权限的工程ID列表
     */
    List<Long> selectProjectIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否有某个工程的权限（返回boolean）
     */
    boolean existsByUserIdAndProjectId(@Param("userId") Long userId,
                                       @Param("projectId") Long projectId);

    /**
     * 插入单条权限记录
     */
    int insert(SysUserProjectPermDO perm);

    /**
     * 批量插入用户工程权限
     */
    int batchInsert(@Param("list") List<SysUserProjectPermDO> list);

    /**
     * 删除用户的所有工程权限
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除项目的所有用户权限（删除项目时级联删除）
     */
    int deleteByProjectId(@Param("projectId") Long projectId);

}