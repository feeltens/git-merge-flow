package com.feeltens.git.oapi.dto.req;

import lombok.Data;

/**
 * GetBranch - 查询分支信息 入参
 */
@Data
public class GetBranchReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * 分支名称
     */
    private String branchName;

}