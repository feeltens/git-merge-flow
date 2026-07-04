package com.feeltens.git.service;

import com.feeltens.git.dto.conflict.ConflictFileContent;
import com.feeltens.git.dto.conflict.GitCredentials;
import com.feeltens.git.dto.conflict.MergeResult;

import java.util.List;

/**
 * JGit 服务接口 - 本地 Git 操作
 *
 * @author feeltens
 */
public interface JGitService {

    /**
     * 执行合并操作（不自动提交）
     *
     * @param localRepoPath 本地仓库路径
     * @param sourceBranch  源分支
     * @param targetBranch  目标分支
     * @return 合并结果（包含冲突信息）
     */
    MergeResult merge(String localRepoPath, String sourceBranch, String targetBranch);

    /**
     * 获取冲突文件列表
     *
     * @param localRepoPath 本地仓库路径
     * @return 冲突文件路径列表
     */
    List<String> getConflictFiles(String localRepoPath);

    /**
     * 读取文件的三个版本（Base, Ours, Theirs）及冲突内容
     *
     * @param localRepoPath 本地仓库路径
     * @param filePath      文件路径
     * @return 冲突文件内容
     */
    ConflictFileContent getConflictFileContent(String localRepoPath, String filePath);

    /**
     * 写入解决后的文件内容
     *
     * @param localRepoPath   本地仓库路径
     * @param filePath        文件路径
     * @param resolvedContent 解决后的内容
     */
    void writeResolvedContent(String localRepoPath, String filePath, String resolvedContent);

    /**
     * 标记冲突已解决并提交
     *
     * @param localRepoPath 本地仓库路径
     * @param commitMessage 提交信息
     * @param authorName    作者名称
     * @param authorEmail   作者邮箱
     */
    void commitResolved(String localRepoPath, String commitMessage, String authorName, String authorEmail);

    /**
     * 推送到远程仓库
     *
     * @param localRepoPath 本地仓库路径
     * @param credentials   Git凭证
     * @param targetBranch  目标分支
     */
    void push(String localRepoPath, GitCredentials credentials, String targetBranch);

    /**
     * 清理临时仓库
     *
     * @param projectName 项目名称
     * @param sessionId   会话ID
     */
    void cleanup(String projectName, String sessionId);

    /**
     * 清理指定项目的所有临时仓库
     *
     * @param projectName 项目名称
     */
    void cleanupByProjectName(String projectName);

    /**
     * 清理所有过期的临时仓库
     *
     * @param expireHours 过期小时数
     */
    void cleanupExpired(int expireHours);

    /**
     * 完整克隆远程仓库（用于缓存）
     * 克隆所有分支和完整历史，不使用浅克隆
     *
     * @param repoUrl     仓库URL
     * @param projectName 项目名称
     * @param cacheId     缓存标识
     * @param credentials Git凭证
     * @return 本地仓库路径
     */
    String cloneFullRepository(String repoUrl, String projectName, String cacheId,
                               GitCredentials credentials);

    /**
     * Fetch 所有远程分支
     *
     * @param localRepoPath 本地仓库路径
     * @param credentials   Git凭证
     */
    void fetch(String localRepoPath, GitCredentials credentials);

    /**
     * 重置本地分支到远程分支
     *
     * @param localRepoPath 本地仓库路径
     * @param branchName    分支名称
     */
    void resetToRemote(String localRepoPath, String branchName);

}