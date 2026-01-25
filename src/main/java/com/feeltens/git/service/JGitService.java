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
     * 克隆仓库到临时目录（浅克隆指定分支）
     *
     * @param repoUrl      仓库URL
     * @param projectName  项目名称
     * @param sessionId    会话ID
     * @param credentials  Git凭证
     * @param sourceBranch 源分支名称
     * @param targetBranch 目标分支名称
     * @return 本地仓库路径
     */
    String cloneRepository(String repoUrl, String projectName, String sessionId,
                           GitCredentials credentials, String sourceBranch, String targetBranch);

    /**
     * 浅克隆+稀疏检出指定文件（优化方案）
     *
     * @param repoUrl      仓库URL
     * @param projectName  项目名称
     * @param sessionId    会话ID
     * @param credentials  Git凭证
     * @param sourceBranch 源分支名称
     * @param targetBranch 目标分支名称
     * @param sparseFiles  需要检出的文件路径列表（从API获取）
     * @return 本地仓库路径
     */
    String cloneRepositoryWithSparseCheckout(String repoUrl, String projectName, String sessionId,
                                             GitCredentials credentials, String sourceBranch,
                                             String targetBranch, List<String> sparseFiles);

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
     * 检查本地仓库是否存在
     *
     * @param projectName 项目名称
     * @param sessionId   会话ID
     * @return 是否存在
     */
    boolean exists(String projectName, String sessionId);

}