package com.feeltens.git.oapi.dto.req;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * CreateBranch - 创建分支 入参
 */
@Data
@FieldNameConstants
public class CreateBranchReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * 创建的分支名称
     */
    private String branch;

    /**
     * 来源分支名称
     */
    private String ref;

}