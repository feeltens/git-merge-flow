package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建git原始分支 入参
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class CreateGitBranchReqVO implements Serializable {

    private static final long serialVersionUID = -2096079844638503850L;

    /**
     * git工程id
     */
    private Long gitProjectId;

    /**
     * 分支名称
     */
    private String gitBranchName;

    /**
     * 分支描述
     */
    private String branchDesc;

    /**
     * 创建来源分支名称
     */
    private String sourceBranch;

    /**
     * 操作人
     */
    private String operator;

}