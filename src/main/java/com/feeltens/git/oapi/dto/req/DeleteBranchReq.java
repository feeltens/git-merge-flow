package com.feeltens.git.oapi.dto.req;

import lombok.Data;

/**
 * DeleteBranch - 删除分支 入参
 */
@Data
public class DeleteBranchReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * 分支名称
     */
    private String branchName;

}