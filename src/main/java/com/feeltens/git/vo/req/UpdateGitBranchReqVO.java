package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改git原始分支 入参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class UpdateGitBranchReqVO {

    /**
     * 分支id
     */
    private Long branchId;

    /**
     * 分支备注
     */
    private String branchDesc;

    /**
     * 操作人
     */
    private String operator;

}