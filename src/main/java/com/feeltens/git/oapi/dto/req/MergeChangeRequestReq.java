package com.feeltens.git.oapi.dto.req;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * MergeChangeRequest - 合并合并请求 入参
 */
@Data
@FieldNameConstants
public class MergeChangeRequestReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * (git服务平台)局部ID，表示代码库中第几个合并请求 （git服务平台的合并请求id）
     */
    private Long localId;

    /**
     * 合并提交信息
     */
    private String mergeMessage = "merge by gitMergeFlow";

    /**
     * 合并类型：
     * ff-only - fast-forward-only 合并方式；
     * no-fast-forward - 普通合并方式；
     * squash - 压缩合并方式；
     * rebase - rebase 合并方式
     */
    private String mergeType = "no-fast-forward";

    /**
     * 是否在合并后删除源分支
     */
    private Boolean removeSourceBranch = false;

}