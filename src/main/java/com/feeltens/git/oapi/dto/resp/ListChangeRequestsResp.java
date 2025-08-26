// package com.feeltens.git.oapi.dto.resp;
//
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.experimental.FieldNameConstants;
//
// import java.util.Date;
// import java.util.List;
//
// /**
//  * ListChangeRequests - 查询合并请求列表 出参
//  */
// @NoArgsConstructor
// @AllArgsConstructor
// @Data
// @Builder(toBuilder = true)
// public class ListChangeRequestsResp {
//
//     private List<ListChangeRequestsResp.ListChangeRequestsItem> requestItemList;
//
//     /*
//     [
//         {
//             "author": {
//                 "avatar": "https://tcs-devops.aliyun.com/thumbnail/111uae6f310d17e0cfa904b28f5d1ff63/w/100/h/100",
//                 "email": "xxx@163.com",
//                 "id": 138573432565,
//                 "name": "tom",
//                 "state": "active",
//                 "userId": "5ed8cca289c20c8bfcd36e13",
//                 "username": "aliyun:tom"
//             },
//             "changeSizeBucket": "XS",
//             "createdAt": "2025-08-17T15:00:08+08:00",
//             "creationMethod": "WEB",
//             "description": "GitMergeFlow: merge branch demo1 into master",
//             "detailUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-code-group/demo-git/change/1",
//             "hasConflict": false,
//             "labels": [],
//             "localId": 1,
//             "mrBizId": "84850a8ef7794e1eb3e8b4b24ff68fcc",
//             "nameWithNamespace": "yourOrganizationId / demo-code-group / demo-git",
//             "projectId": 5603183,
//             "reviewers": [],
//             "sourceBranch": "demo1",
//             "sourceProjectId": 5603183,
//             "sourceType": "BRANCH",
//             "state": "TO_BE_MERGED",
//             "supportMergeFFOnly": false,
//             "targetBranch": "master",
//             "targetProjectId": 5603183,
//             "targetType": "BRANCH",
//             "title": "GitMergeFlow: merge branch demo1 into master",
//             "totalCommentCount": 0,
//             "unResolvedCommentCount": 0,
//             "updatedAt": "2025-08-17T15:00:08+08:00",
//             "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-code-group/demo-git",
//             "workInProgress": false
//         }
//     ]
//     */
//
//     @NoArgsConstructor
//     @AllArgsConstructor
//     @Data
//     @Builder(toBuilder = true)
//     @FieldNameConstants
//     public static class ListChangeRequestsItem {
//
//         /**
//          * git服务平台的合并请求id
//          */
//         private Long localId;
//         /**
//          * <pre>
//          *     gitlab
//          *     https://docs.gitlab.com/18.3/api/merge_requests/#list-merge-requests
//          *
//          *     codeup
//          *     https://help.aliyun.com/zh/yunxiao/developer-reference/listchangerequests-query-the-list-of-merge-requests
//          * </pre>
//          */
//         // private Boolean hasConflict;
//         /**
//          * <pre>
//          *     gitlab
//          *     https://docs.gitlab.com/18.3/api/merge_requests/#list-merge-requests
//          *
//          *     codeup
//          *     https://help.aliyun.com/zh/yunxiao/developer-reference/listchangerequests-query-the-list-of-merge-requests
//          * </pre>
//          */
//         // private String state;
//
//          /**
//            * 合并总状态
//            * com.feeltens.git.enums.MergeTotalStatusEnum status
//            * <pre>
//            *     1、gitlab
//            *     https://docs.gitlab.com/18.3/api/merge_requests/#create-mr
//            *     com.feeltens.git.oapi.factory.GitLabOpenapiProcess#getMergeTotalStatus
//            *
//            *     2、codeup
//            *     https://help.aliyun.com/zh/yunxiao/developer-reference/createchangerequest-create-merge-request
//            *     com.feeltens.git.oapi.factory.CodeupOpenapiProcess#getMergeTotalStatus
//            * </pre>
//          */
//         private Integer mergeTotalStatus;
//
//         private Long projectId;
//         private String sourceBranch;
//         private String targetBranch;
//         private String title;
//         private String description;
//
//         private Date createdAt;
//         private Date updatedAt;
//
//     }
//
// }