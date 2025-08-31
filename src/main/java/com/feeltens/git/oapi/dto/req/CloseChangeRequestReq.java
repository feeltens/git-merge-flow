package com.feeltens.git.oapi.dto.req;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * CloseChangeRequest - 关闭合并请求 入参
 */
@Data
@FieldNameConstants
public class CloseChangeRequestReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * (git服务平台)局部ID，表示代码库中第几个合并请求 （git服务平台的合并请求id）
     */
    private Long localId;

}