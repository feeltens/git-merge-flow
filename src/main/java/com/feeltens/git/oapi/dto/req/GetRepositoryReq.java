package com.feeltens.git.oapi.dto.req;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * GetRepository - 查询代码库 入参
 */
@Data
@FieldNameConstants
public class GetRepositoryReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

}