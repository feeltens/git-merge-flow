package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询git原始分支 入参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ListGitBranchReqVO {

    /**
     * git工程id
     */
    private Long gitProjectId;

    /**
     * 中间分支id
     */
    private Long mixBranchId;

    /**
     * 分支名称
     */
    private String branchName;

}