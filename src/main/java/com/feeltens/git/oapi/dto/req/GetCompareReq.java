package com.feeltens.git.oapi.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * GetCompare - 查询代码比较内容 入参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@FieldNameConstants
public class GetCompareReq extends OapiBaseReq {

    /**
     * (git服务平台)仓库ID
     */
    private Long repositoryId;

    /**
     * 判断 test1 分支是否已合并进 master 分支，则 from=master, to=test1
     * <p>
     * master
     */
    private String from;

    /**
     * test1
     */
    private String to;

    /**
     * 可选值：branch、tag；
     * 若是 commit 比较，可不传；
     * 若是分支比较，则需传入：branch，亦可不传，但需要确保不存在分支或 tag 重名的情况；
     * 若是 tag 比较，则需传入：tag；
     * 若是存在分支和标签同名的情况，则需要严格传入 branch 或者 tag。
     */
    private String sourceType = "branch";

    private String targetType = "branch";

}