// package com.feeltens.git.oapi.dto.req;
//
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.experimental.FieldNameConstants;
//
// /**
//  * ListChangeRequests - 查询合并请求列表 入参
//  */
// @NoArgsConstructor
// @AllArgsConstructor
// @Data
// @Builder(toBuilder = true)
// @FieldNameConstants
// public class ListChangeRequestsReq extends OapiBaseReq {
//
//     /**
//      * (git服务平台)仓库ID
//      */
//     private Long repositoryId;
//
//     /**
//      * 代码库 ID 或者路径列表，多个以逗号分隔
//      * <pre>
//      *     eg:
//      *     2308912,2308913
//      * </pre>
//      */
//     private String projectIds;
//
//     /**
//      * 合并请求筛选状态
//      * <pre>
//      *     1、gitlab
//      *     https://docs.gitlab.com/18.3/api/merge_requests/#list-merge-requests
//      *          opened
//      *          closed
//      *          locked
//      *          merged
//      *          all 所有状态
//      *
//      *     2、codeup
//      *     https://help.aliyun.com/zh/yunxiao/developer-reference/listchangerequests-query-the-list-of-merge-requests
//      *          opened 已打开
//      *          merged 已合并
//      *          closed 已关闭
//      *          默认为 null，即查询全部状态。
//      * </pre>
//      */
//     private String state;
//
// }