package com.feeltens.git.vo.req;

import lombok.Data;

/**
 * 初始化冲突解决请求
 *
 * @author feeltens
 */
@Data
public class InitConflictReqVO {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 中间分支ID（可选）
     */
    private Long mixBranchId;

    /**
     * 源分支名
     */
    private String sourceBranch;

    /**
     * 目标分支名
     */
    private String targetBranch;

    /**
     * 操作人
     */
    private String operator;

}