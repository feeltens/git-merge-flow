package com.feeltens.git.oapi.dto.req;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * CreateChangeRequest - 创建合并请求 入参
 */
@Data
@FieldNameConstants
public class CreateChangeRequestReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * 源分支
     */
    private String sourceBranch;
    /**
     * 源仓库ID
     */
    private Long sourceProjectId;

    /**
     * 目标分支
     */
    private String targetBranch;
    /**
     * 目标仓库ID
     */
    private Long targetProjectId;

    /**
     * 标题
     * 不超过256个字符
     * <pre>
     *     eg:
     *     "title": "GitMergeFlow: merge branch demo1 into master"
     * </pre>
     */
    private String title;
    /**
     * 描述
     * 不超过10000个字符
     */
    private String description;

}