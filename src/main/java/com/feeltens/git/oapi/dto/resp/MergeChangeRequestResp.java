package com.feeltens.git.oapi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * MergeChangeRequest - 合并合并请求 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class MergeChangeRequestResp {

    /*
    {
        "ahead": 0,
        "allRequirementsPass": true,
        "author": {
            "avatar": "https://tcs-devops.aliyun.com/thumbnail/111uae6f310d17e0cfa904b28f5d1ff63/w/100/h/100",
            "email": "xxx@163.com",
            "id": 138573432565,
            "name": "tom",
            "state": "active",
            "userId": "5ed8cca289c20c8bfcd36e13",
            "username": "aliyun:tom"
        },
        "behind": 1,
        "bizId": "84850a8ef7794e1eb3e8b4b24ff68fcc",
        "canRevertOrCherryPick": true,
        "changeSizeBucket": "XS",
        "checkList": {
            "needAttentionItems": [],
            "requirementRuleItems": []
        },
        "createFrom": "WEB",
        "createTime": "2025-08-17T15:00:08+08:00",
        "description": "GitMergeFlow: merge branch demo1 into master",
        "detailUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-code-group/demo-git/change/1",
        "hasReverted": false,
        "localId": 1,
        "mergedRevision": "e19d618b50d0ef7efea2976272d32bd048b1ab34",
        "mrType": "CODE_REVIEW",
        "projectId": 5603183,
        "reviewers": [],
        "sourceBranch": "demo1",
        "sourceProjectId": 5603183,
        "status": "MERGED",
        "subscribers": [],
        "supportMergeFastForwardOnly": false,
        "targetBranch": "master",
        "targetProjectId": 5603183,
        "targetProjectNameWithNamespace": "yourOrganizationId / demo-code-group / demo-git",
        "targetProjectPathWithNamespace": "yourOrganizationId/demo-code-group/demo-git",
        "title": "GitMergeFlow: merge branch demo1 into master",
        "totalCommentCount": 0,
        "unResolvedCommentCount": 0,
        "updateTime": "2025-08-17T15:01:12+08:00",
        "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-code-group/demo-git"
    }
    */

    /**
     * git服务平台的合并请求id
     */
    private Long localId;

    /**
     * 合并总状态
     * com.feeltens.git.enums.MergeTotalStatusEnum status
     * <pre>
     *     1、gitlab
     *     https://docs.gitlab.com/18.3/api/merge_requests/#create-mr
     *     com.feeltens.git.oapi.factory.GitLabOpenapiProcess#getMergeTotalStatus
     *
     *     2、codeup
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/createchangerequest-create-merge-request
     *     com.feeltens.git.oapi.factory.CodeupOpenapiProcess#getMergeTotalStatus
     * </pre>
     */
    private Integer mergeTotalStatus;

    /**
     * (git服务平台)仓库ID
     */
    private Long projectId;
    private String sourceBranch;
    private String targetBranch;
    private String title;
    private String description;

    private Date createTime;
    private Date updateTime;

}