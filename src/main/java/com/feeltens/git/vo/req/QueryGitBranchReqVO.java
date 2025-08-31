package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个查询git原始分支 入参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class QueryGitBranchReqVO {

    /**
     * git原始分支id
     */
    private Long branchId;

}