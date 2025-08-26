package com.feeltens.git.oapi.dto.resp;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * GetChangeRequest - 查询合并请求 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class GetChangeRequestResp {

    /*
    {
        "ahead": 1,
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
        "behind": 0,
        "bizId": "b9740f330e0247d68f802da1dc386aa0",
        "canRevertOrCherryPick": false,
        "changeSizeBucket": "XS",
        "checkList": {
            "needAttentionItems": [],
            "requirementRuleItems": [
                {
                    "itemType": "MERGE_CONFLICT_CHECK",
                    "pass": true
                }
            ]
        },
        "createFrom": "WEB",
        "createTime": "2025-08-17T08:32:30+08:00",
        "description": "GitMergeFlow: merge branch test4 into master",
        "detailUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo/change/5",
        "hasReverted": false,
        "localId": 5,
        "mrType": "CODE_REVIEW",
        "projectId": 5600997,
        "reviewers": [],
        "sourceBranch": "test4",
        "sourceProjectId": 5600997,
        "status": "TO_BE_MERGED",
        "subscribers": [],
        "supportMergeFastForwardOnly": true,
        "targetBranch": "master",
        "targetProjectId": 5600997,
        "targetProjectNameWithNamespace": "yourOrganizationId / demo-repo",
        "targetProjectPathWithNamespace": "yourOrganizationId/demo-repo",
        "title": "GitMergeFlow: merge branch test4 into master",
        "totalCommentCount": 0,
        "unResolvedCommentCount": 0,
        "updateTime": "2025-08-17T08:32:30+08:00",
        "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo"
    }
    */

    /**
     * 局部ID （git服务平台的合并请求id）
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

    private Long projectId;
    private String sourceBranch;
    private String targetBranch;
    private String title;
    private String description;

    private Date createTime;
    private Date updateTime;

    /**
     * "diff_refs": {
     * "base_sha": "d04a0e406ac012d4e52013c5e0e088907de2f08b",
     * "head_sha": "d04a0e406ac012d4e52013c5e0e088907de2f08b",
     * "start_sha": "d04a0e406ac012d4e52013c5e0e088907de2f08b"
     * }
     */
    private JSONObject diffRefsObj;

}