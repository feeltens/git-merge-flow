package com.feeltens.git.oapi.dto.req;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * ListBranches - 查询分支列表 入参
 */
@Data
@FieldNameConstants
public class ListBranchesReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

}