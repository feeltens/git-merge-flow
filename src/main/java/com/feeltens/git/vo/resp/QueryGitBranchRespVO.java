package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 单个查询git原始分支 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class QueryGitBranchRespVO {

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

}