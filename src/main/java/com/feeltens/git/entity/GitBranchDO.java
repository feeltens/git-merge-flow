package com.feeltens.git.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * git原始分支
 *
 * @author feeltens
 * @date 2025-08-17
 *
 * <pre>
 * 表名：git_branch
 * </pre>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class GitBranchDO implements Serializable {

    /**
     * 分支id
     */
    private Long branchId;

    /**
     * 分支名称
     */
    private String branchName;

    /**
     * 工程id
     */
    private Long projectId;

    /**
     * 分支备注
     */
    private String branchDesc;

    /**
     * 是否是默认分支 默认为0-非默认分支；1-默认分支
     */
    private Integer defaultBranchFlag;

    /**
     * 已合并进master状态 默认为0-未合并进master；1-已合并进master
     */
    @Deprecated
    private Integer mergedMasterFlag;

    /**
     * 创建来源分支名称
     */
    private String sourceBranch;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 最近一次提交时间
     */
    private Date lastCommitTime;

    /**
     * 最近一次提交人
     */
    private String lastCommitUser;

    /**
     * 最近一次提交人邮箱
     */
    private String lastCommitEmail;

    /**
     * 最近一次提交ID
     */
    private String lastCommitId;

    /**
     * 最近一次提交shortID
     */
    private String lastCommitShortId;

    /**
     * 最近一次提交内容
     */
    private String lastCommitMessage;

    /**
     * 删除标识: 0代表未删除，>0代表已删除，此时同branch_id
     */
    private Long deleted;

    /**
     * 分支ext扩展信息
     */
    private String branchExt;

    private static final long serialVersionUID = 1L;

}