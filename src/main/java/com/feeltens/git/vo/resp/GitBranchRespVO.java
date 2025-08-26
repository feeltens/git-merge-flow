package com.feeltens.git.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询git原始分支 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class GitBranchRespVO implements Serializable {

    private static final long serialVersionUID = -4774233129806037444L;

    /**
     * 分支id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long branchId;

    /**
     * 分支名称
     */
    private String branchName;

    /**
     * 工程id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long projectId;

    /**
     * 分支备注
     */
    private String branchDesc;

    /**
     * 已合并进master状态 默认为0-未合并进master；1-已合并进master
     */
    // private Integer mergedMasterFlag;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 最近一次提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * 集成状态：0-失败；1-成功；-1-未集成
     */
    private Integer mergeStatus;

}