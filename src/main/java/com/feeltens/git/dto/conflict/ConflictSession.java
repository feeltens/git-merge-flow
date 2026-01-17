package com.feeltens.git.dto.conflict;

import com.feeltens.git.enums.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 冲突解决会话
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictSession {

    /**
     * 会话ID (UUID)
     */
    private String sessionId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 仓库ID
     */
    private Long repositoryId;

    /**
     * 组织ID
     */
    private String organizationId;

    /**
     * 仓库 URL
     */
    private String repoUrl;

    /**
     * 源分支
     */
    private String sourceBranch;

    /**
     * 目标分支
     */
    private String targetBranch;

    /**
     * 本地仓库路径
     */
    private String localRepoPath;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 会话状态
     */
    private SessionStatus status;

    /**
     * 冲突文件列表
     */
    private List<String> conflictFiles;

    /**
     * 已解决的文件内容缓存 (filePath -> resolvedContent)
     */
    @Builder.Default
    private Map<String, String> resolvedContents = new ConcurrentHashMap<>();

    /**
     * 原始冲突内容缓存 (filePath -> originalMergedContent)
     * 用于重置功能，保存初始的包含冲突标记的内容
     */
    @Builder.Default
    private Map<String, String> originalConflictContents = new ConcurrentHashMap<>();

    /**
     * 中间分支ID（可选，用于更新合并状态）
     */
    private Long mixBranchId;

    /**
     * 错误信息
     */
    private String errorMessage;

}