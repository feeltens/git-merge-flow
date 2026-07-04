package com.feeltens.git.service;

import com.feeltens.git.common.exception.BizException;

/**
 * 仓库缓存服务接口
 * 负责管理远程仓库的本地缓存，包括完整克隆、主分支同步、本地拷贝等功能
 *
 * @author feeltens
 */
public interface RepoCacheService {

    /**
     * 完整克隆远程仓库到本地缓存
     * 克隆所有分支和完整历史，不使用浅克隆
     *
     * @param projectId 项目ID
     */
    void cloneFullRepo(Long projectId);

    /**
     * 拉取主分支最新代码
     * 从远程仓库拉取主分支的最新提交到本地缓存
     *
     * @param projectId 项目ID
     */
    void pullMainBranch(Long projectId);

    /**
     * 拷贝到会话目录
     * 从缓存目录拷贝项目到指定的会话目录
     *
     * @param projectId 项目ID
     * @param sessionId 会话ID
     * @return 会话目录的本地路径
     */
    String copyToSessionDir(Long projectId, String sessionId);

    /**
     * 确保缓存就绪
     * 在冲突解决前调用，确保本地缓存是最新的
     * 1. 检查克隆状态
     * 2. 如果未克隆，抛出异常
     * 3. 触发主分支拉取（同步执行）
     * 4. 等待拉取完成并验证结果
     *
     * @param projectId 项目ID
     * @throws BizException 如果缓存未就绪或拉取失败
     */
    void ensureCacheReady(Long projectId);

    /**
     * 获取缓存状态
     *
     * @param projectId 项目ID
     * @return 克隆状态字符串
     */
    String getCacheStatus(Long projectId);

    /**
     * 清理缓存
     * 删除指定项目的本地缓存目录
     *
     * @param projectId 项目ID
     */
    void cleanupCache(Long projectId);

}