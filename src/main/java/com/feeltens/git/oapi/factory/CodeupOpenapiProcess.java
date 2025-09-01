package com.feeltens.git.oapi.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feeltens.git.enums.GitServiceEnum;
import com.feeltens.git.enums.MergeTotalStatusEnum;
import com.feeltens.git.oapi.dto.req.CloseChangeRequestReq;
import com.feeltens.git.oapi.dto.req.CreateBranchReq;
import com.feeltens.git.oapi.dto.req.CreateChangeRequestReq;
import com.feeltens.git.oapi.dto.req.DeleteBranchReq;
import com.feeltens.git.oapi.dto.req.GetBranchReq;
import com.feeltens.git.oapi.dto.req.GetChangeRequestReq;
import com.feeltens.git.oapi.dto.req.GetCompareReq;
import com.feeltens.git.oapi.dto.req.GetRepositoryReq;
import com.feeltens.git.oapi.dto.req.ListBranchesReq;
import com.feeltens.git.oapi.dto.req.ListOrganizationsReq;
import com.feeltens.git.oapi.dto.req.ListRepositoriesReq;
import com.feeltens.git.oapi.dto.req.MergeChangeRequestReq;
import com.feeltens.git.oapi.dto.resp.CloseChangeRequestResp;
import com.feeltens.git.oapi.dto.resp.CreateBranchResp;
import com.feeltens.git.oapi.dto.resp.CreateChangeRequestResp;
import com.feeltens.git.oapi.dto.resp.DeleteBranchResp;
import com.feeltens.git.oapi.dto.resp.GetBranchResp;
import com.feeltens.git.oapi.dto.resp.GetChangeRequestResp;
import com.feeltens.git.oapi.dto.resp.GetCompareResp;
import com.feeltens.git.oapi.dto.resp.GetRepositoryResp;
import com.feeltens.git.oapi.dto.resp.ListBranchesResp;
import com.feeltens.git.oapi.dto.resp.ListOrganizationsResp;
import com.feeltens.git.oapi.dto.resp.ListRepositoriesResp;
import com.feeltens.git.oapi.dto.resp.MergeChangeRequestResp;
import com.feeltens.git.util.DateTimeXUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Codeup Oapi 工具类
 */
@Service
@Slf4j
public class CodeupOpenapiProcess implements GitOpenApiProcess {

    private static final String HEADER_TOKEN_KEY = "x-yunxiao-token";
    private static final String ERROR_CODE = "errorCode";
    private static final String NOT_NEED_MERGE_RESPONSE_MSG = "源分支相对目标分支没有改动";

    /**
     * 获取git服务平台枚举
     */
    @Override
    public GitServiceEnum getGitServiceEnum() {
        return GitServiceEnum.CODEUP;
    }

    /**
     * ListOrganizations - 查询组织列表
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/listorganizations
     *
     *      /oapi/v1/platform/organizations
     * </pre>
     */
    public ListOrganizationsResp listOrganizations(ListOrganizationsReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/platform/organizations";
        url = url.replace("{baseUrl}", req.getBaseUrl());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("listOrganizations codeup hasError, e:", e);
        }
        log.info("listOrganizations codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new ListOrganizationsResp();
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("listOrganizations codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        List<JSONObject> list = JSON.parseArray(responseBody, JSONObject.class);
        List<ListOrganizationsResp.ListOrganizationsRespItem> organazationItemList = Lists.newArrayList();
        for (JSONObject jsonObject : list) {
            ListOrganizationsResp.ListOrganizationsRespItem repoItem = ListOrganizationsResp.ListOrganizationsRespItem.builder()
                    .id(jsonObject.getString(ListOrganizationsResp.ListOrganizationsRespItem.Fields.id))
                    .name(jsonObject.getString(ListOrganizationsResp.ListOrganizationsRespItem.Fields.name))
                    .createTime(DateTimeXUtil.isoString2Date(jsonObject.getString("created")))
                    .updateTime(DateTimeXUtil.isoString2Date(jsonObject.getString("updated")))
                    .build();
            organazationItemList.add(repoItem);
        }
        return new ListOrganizationsResp(organazationItemList);
    }

    /**
     * ListRepositories - 查询代码库列表
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/listrepositories-query-code-base-list
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories
     * </pre>
     */
    public ListRepositoriesResp listRepositories(ListRepositoriesReq req) {
        int page = 1;
        List<ListRepositoriesResp.ListRepositoriesRespItem> resultList = Lists.newArrayList();
        // 最大100w调用，防止死循环
        for (int i = 0; i < 100_0000; i++) {
            ListRepositoriesResp resp = doListRepositories(req, page);
            if (null != resp && CollUtil.isNotEmpty(resp.getRepoItemList())) {
                CollUtil.addAll(resultList, resp.getRepoItemList());
                page++;
            } else {
                break;
            }
        }
        return new ListRepositoriesResp(resultList);
    }

    private ListRepositoriesResp doListRepositories(ListRepositoriesReq req, int page) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        if (StrUtil.isNotEmpty(req.getSearch())) {
            formMap.put(ListRepositoriesReq.Fields.search, req.getSearch());
        }
        formMap.put("page", page); // 页码，默认从 1 开始，一般不要超过 150 页。
        formMap.put("perPage", 100); // 每页大小，默认 20，取值范围【1，100】。

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .form(formMap)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("listRepositories codeup hasError, e:", e);
        }
        log.info("listRepositories codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new ListRepositoriesResp();
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("listRepositories codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        List<JSONObject> list = JSON.parseArray(responseBody, JSONObject.class);
        List<ListRepositoriesResp.ListRepositoriesRespItem> repoItemList = Lists.newArrayList();
        for (JSONObject jsonObject : list) {
            ListRepositoriesResp.ListRepositoriesRespItem repoItem = ListRepositoriesResp.ListRepositoriesRespItem.builder()
                    .id(jsonObject.getLong(ListRepositoriesResp.ListRepositoriesRespItem.Fields.id))
                    .name(jsonObject.getString(ListRepositoriesResp.ListRepositoriesRespItem.Fields.name))
                    .createdAt(DateTimeXUtil.isoString2Date(jsonObject.getString(ListRepositoriesResp.ListRepositoriesRespItem.Fields.createdAt)))
                    .updatedAt(DateTimeXUtil.isoString2Date(jsonObject.getString(ListRepositoriesResp.ListRepositoriesRespItem.Fields.updatedAt)))
                    .build();
            repoItemList.add(repoItem);
        }
        return new ListRepositoriesResp(repoItemList);
    }

    /**
     * GetRepository - 查询代码库
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/getrepository-query-the-code-base
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}
     * </pre>
     */
    public GetRepositoryResp getRepository(GetRepositoryReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("getRepository codeup hasError, e:", e);
        }
        log.info("getRepository codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new GetRepositoryResp();
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("getRepository codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return GetRepositoryResp.builder()
                .id(jsonObject.getLong(GetRepositoryResp.Fields.id))
                .name(jsonObject.getString(GetRepositoryResp.Fields.name))
                .defaultBranch(jsonObject.getString(GetRepositoryResp.Fields.defaultBranch))
                .createdAt(DateTimeXUtil.isoString2Date(jsonObject.getString(GetRepositoryResp.Fields.createdAt)))
                .updatedAt(DateTimeXUtil.isoString2Date(jsonObject.getString(GetRepositoryResp.Fields.updatedAt)))
                .httpUrlToRepo(jsonObject.getString(GetRepositoryResp.Fields.httpUrlToRepo))
                .sshUrlToRepo(jsonObject.getString(GetRepositoryResp.Fields.sshUrlToRepo))
                .webUrl(jsonObject.getString(GetRepositoryResp.Fields.webUrl))
                .build();
    }

    /**
     * CreateBranch - 创建分支
     * <pre>
     *      https://help.aliyun.com/zh/yunxiao/developer-reference/createbranch-create-branch
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches
     * </pre>
     */
    public CreateBranchResp createBranch(CreateBranchReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put(CreateBranchReq.Fields.branch, req.getBranch());
        formMap.put(CreateBranchReq.Fields.ref, req.getRef());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createPost(url)
                    .addHeaders(headers)
                    .form(formMap)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("createBranch codeup hasError, e:", e);
        }
        log.info("createBranch codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("createBranch openApi failed with nothing");
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("createBranch codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        JSONObject commitJsonObj = jsonObject.getObject(CreateBranchResp.Fields.commit, JSONObject.class);
        return CreateBranchResp.builder()
                .name(jsonObject.getString(CreateBranchResp.Fields.name))
                .authorName(commitJsonObj.getString(CreateBranchResp.Fields.authorName))
                .authorEmail(commitJsonObj.getString(CreateBranchResp.Fields.authorEmail))
                .committedDate(DateTimeXUtil.isoString2Date(commitJsonObj.getString(CreateBranchResp.Fields.committedDate)))
                .webUrl(jsonObject.getString(CreateBranchResp.Fields.webUrl))
                .lastCommitTime(DateTimeXUtil.isoString2Date(commitJsonObj.getString(GetBranchResp.Fields.committedDate)))
                .lastCommitUser(commitJsonObj.getString("authorName"))
                .lastCommitEmail(commitJsonObj.getString("committerEmail"))
                .lastCommitId(commitJsonObj.getString("id"))
                .lastCommitShortId(commitJsonObj.getString("shortId"))
                .lastCommitMessage(commitJsonObj.getString("message"))
                .build();
    }

    private String getEncodeStr(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * GetBranch - 查询分支信息
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/getbranch-query-branch-information
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches/{branchName}
     * </pre>
     */
    public GetBranchResp getBranch(GetBranchReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches/{branchName}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());
        url = url.replace("{branchName}", getEncodeStr(req.getBranchName()));

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("getBranch codeup hasError, e:", e);
        }
        log.info("getBranch codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("getBranch openApi failed with nothing");
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("getBranch codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        JSONObject commitJsonObj = jsonObject.getObject(GetBranchResp.Fields.commit, JSONObject.class);
        return GetBranchResp.builder()
                .name(jsonObject.getString(GetBranchResp.Fields.name))
                .authorName(commitJsonObj.getString(GetBranchResp.Fields.authorName))
                .authorEmail(commitJsonObj.getString(GetBranchResp.Fields.authorEmail))
                .committedDate(DateTimeXUtil.isoString2Date(commitJsonObj.getString(GetBranchResp.Fields.committedDate)))
                .webUrl(jsonObject.getString(GetBranchResp.Fields.webUrl))
                .build();
    }

    /**
     * ListBranches - 查询分支列表
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/listbranches-query-the-list-of-branches
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches
     * </pre>
     */
    public ListBranchesResp listBranches(ListBranchesReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("listBranches codeup hasError, e:", e);
        }
        log.info("listBranches codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new ListBranchesResp();
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("listBranches codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        List<JSONObject> list = JSON.parseArray(responseBody, JSONObject.class);
        List<ListBranchesResp.BranchItem> branchItemList = Lists.newArrayList();
        for (JSONObject jsonObject : list) {
            JSONObject commitJsonObj = jsonObject.getObject(ListBranchesResp.BranchItem.Fields.commit, JSONObject.class);
            ListBranchesResp.BranchItem branchItem = ListBranchesResp.BranchItem.builder()
                    .name(jsonObject.getString(ListBranchesResp.BranchItem.Fields.name))
                    .authorName(commitJsonObj.getString(ListBranchesResp.BranchItem.Fields.authorName))
                    .authorEmail(commitJsonObj.getString(ListBranchesResp.BranchItem.Fields.authorEmail))
                    .committedDate(DateTimeXUtil.isoString2Date(commitJsonObj.getString(ListBranchesResp.BranchItem.Fields.committedDate)))
                    .webUrl(jsonObject.getString(ListBranchesResp.BranchItem.Fields.webUrl))
                    .defaultBranch(jsonObject.getBoolean(ListBranchesResp.BranchItem.Fields.defaultBranch))
                    .lastCommitTime(DateTimeXUtil.isoString2Date(commitJsonObj.getString(ListBranchesResp.BranchItem.Fields.committedDate)))
                    .lastCommitUser(commitJsonObj.getString("authorName"))
                    .lastCommitEmail(commitJsonObj.getString("committerEmail"))
                    .lastCommitId(commitJsonObj.getString("id"))
                    .lastCommitShortId(commitJsonObj.getString("shortId"))
                    .lastCommitMessage(commitJsonObj.getString("message"))
                    .build();
            branchItemList.add(branchItem);
        }
        return new ListBranchesResp(branchItemList);
    }

    /**
     * DeleteBranch - 删除分支
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/deletebranch-delete-branch
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches/{branchName}
     * </pre>
     */
    public DeleteBranchResp deleteBranch(DeleteBranchReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/branches/{branchName}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());
        url = url.replace("{branchName}", getEncodeStr(req.getBranchName()));

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createRequest(Method.DELETE, url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("deleteBranch codeup hasError, e:", e);
        }
        log.info("deleteBranch codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new DeleteBranchResp();
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("deleteBranch codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        String branchName = jsonObject.getString(DeleteBranchResp.Fields.branchName);
        return new DeleteBranchResp(branchName);
    }

    /**
     * CreateChangeRequest - 创建合并请求
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/createchangerequest-create-merge-request
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests
     * </pre>
     */
    public CreateChangeRequestResp createChangeRequest(CreateChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        req.setSourceProjectId(req.getRepositoryId());
        req.setTargetProjectId(req.getRepositoryId());
        String title = String.format("GitMergeFlow: merge branch %s into %s", req.getSourceBranch(), req.getTargetBranch());
        req.setTitle(title);
        req.setDescription(title);

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createPost(url)
                    .addHeaders(headers)
                    .body(JSON.toJSONString(req))
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("createChangeRequest codeup hasError, e:", e);
        }
        log.info("createChangeRequest codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("createChangeRequest openApi failed with nothing");
        }

        if (!responseBody.contains(NOT_NEED_MERGE_RESPONSE_MSG)
                && responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("createChangeRequest codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        String createTimeStr = jsonObject.getString(CreateChangeRequestResp.Fields.createTime);
        String updateTimeStr = jsonObject.getString(CreateChangeRequestResp.Fields.updateTime);
        Date createTime = StrUtil.isNotEmpty(createTimeStr) ? DateTimeXUtil.isoString2Date(createTimeStr) : null;
        Date updateTime = StrUtil.isNotEmpty(updateTimeStr) ? DateTimeXUtil.isoString2Date(updateTimeStr) : null;
        return CreateChangeRequestResp.builder()
                .localId(jsonObject.getLong(CreateChangeRequestResp.Fields.localId))
                .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                .projectId(jsonObject.getLong(CreateChangeRequestResp.Fields.projectId))
                .sourceBranch(jsonObject.getString(CreateChangeRequestResp.Fields.sourceBranch))
                .targetBranch(jsonObject.getString(CreateChangeRequestResp.Fields.targetBranch))
                .title(jsonObject.getString(CreateChangeRequestResp.Fields.title))
                .description(jsonObject.getString(CreateChangeRequestResp.Fields.description))
                .createTime(createTime)
                .updateTime(updateTime)
                .build();
    }

    /**
     * ListChangeRequests - 查询开启状态的合并请求列表
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/listchangerequests-query-the-list-of-merge-requests
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/changeRequests
     * </pre>
     */
    // public ListChangeRequestsResp listOpenedRequests(ListChangeRequestsReq req) {
    //     req.setState("opened");
    //     return listChangeRequests(req);
    // }

    /*public ListChangeRequestsResp listChangeRequests(ListChangeRequestsReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put(ListChangeRequestsReq.Fields.projectIds, req.getProjectIds());
        formMap.put(ListChangeRequestsReq.Fields.state, req.getState());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/changeRequests";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .form(formMap)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("listChangeRequests codeup hasError, e:", e);
        }
        log.info("listChangeRequests codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("listChangeRequests openApi failed with nothing");
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("listChangeRequests codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        List<JSONObject> jsonObjectList = JSON.parseArray(responseBody, JSONObject.class);
        List<ListChangeRequestsResp.ListChangeRequestsItem> requestItemList = Lists.newArrayList();
        for (JSONObject jsonObject : jsonObjectList) {
            ListChangeRequestsResp.ListChangeRequestsItem requestItem = ListChangeRequestsResp.ListChangeRequestsItem.builder()
                    .localId(jsonObject.getLong(ListChangeRequestsResp.ListChangeRequestsItem.Fields.localId))
                    .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                    .projectId(jsonObject.getLong(ListChangeRequestsResp.ListChangeRequestsItem.Fields.projectId))
                    .sourceBranch(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.sourceBranch))
                    .targetBranch(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.targetBranch))
                    .title(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.title))
                    .description(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.description))
                    .createdAt(DateTimeXUtil.isoString2Date(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.createdAt)))
                    .updatedAt(DateTimeXUtil.isoString2Date(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.updatedAt)))
                    .build();
            requestItemList.add(requestItem);
        }

        return new ListChangeRequestsResp(requestItemList);
    }*/

    /**
     * ListMergedRequests - 查询已合并状态的合并请求列表
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/listchangerequests-query-the-list-of-merge-requests
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/changeRequests
     * </pre>
     */
    // public ListChangeRequestsResp listMergedRequests(ListChangeRequestsReq req) {
    //     req.setState("merged");
    //     return listChangeRequests(req);
    // }

    /**
     * GetChangeRequest - 查询合并请求
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/getchangerequest-query-merge-request
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests/{localId}
     * </pre>
     */
    public GetChangeRequestResp getChangeRequest(GetChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests/{localId}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());
        url = url.replace("{localId}", req.getLocalId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("getChangeRequest codeup hasError, e:", e);
        }
        log.info("getChangeRequest codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new GetChangeRequestResp();
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("getChangeRequest codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return GetChangeRequestResp.builder()
                .localId(jsonObject.getLong(GetChangeRequestResp.Fields.localId))
                .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                .openFlag(getOpenFlag(jsonObject))
                .projectId(jsonObject.getLong(GetChangeRequestResp.Fields.projectId))
                .sourceBranch(jsonObject.getString(GetChangeRequestResp.Fields.sourceBranch))
                .targetBranch(jsonObject.getString(GetChangeRequestResp.Fields.targetBranch))
                .title(jsonObject.getString(GetChangeRequestResp.Fields.title))
                .description(jsonObject.getString(GetChangeRequestResp.Fields.description))
                .createTime(DateTimeXUtil.isoString2Date(jsonObject.getString(GetChangeRequestResp.Fields.createTime)))
                .updateTime(DateTimeXUtil.isoString2Date(jsonObject.getString(GetChangeRequestResp.Fields.updateTime)))
                .build();
    }

    /**
     * MergeChangeRequest - 合并合并请求
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/mergechangerequest-merge-merge-request
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests/{localId}/merge
     * </pre>
     */
    public MergeChangeRequestResp mergeChangeRequest(MergeChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests/{localId}/merge";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());
        url = url.replace("{localId}", req.getLocalId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createPost(url)
                    .addHeaders(headers)
                    .body(JSON.toJSONString(new MergeChangeRequestReq()))
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("mergeChangeRequest codeup hasError, e:", e);
        }
        log.info("mergeChangeRequest codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("mergeChangeRequest openApi failed with nothing");
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("mergeChangeRequest codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return MergeChangeRequestResp.builder()
                .localId(jsonObject.getLong(MergeChangeRequestResp.Fields.localId))
                .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                .projectId(jsonObject.getLong(MergeChangeRequestResp.Fields.projectId))
                .sourceBranch(jsonObject.getString(MergeChangeRequestResp.Fields.sourceBranch))
                .targetBranch(jsonObject.getString(MergeChangeRequestResp.Fields.targetBranch))
                .title(jsonObject.getString(MergeChangeRequestResp.Fields.title))
                .description(jsonObject.getString(MergeChangeRequestResp.Fields.description))
                .createTime(DateTimeXUtil.isoString2Date(jsonObject.getString(MergeChangeRequestResp.Fields.createTime)))
                .updateTime(DateTimeXUtil.isoString2Date(jsonObject.getString(MergeChangeRequestResp.Fields.updateTime)))
                .build();
    }

    /**
     * CloseChangeRequest - 关闭合并请求
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/closechangerequest-close-merge-request
     *
     *      /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests/{localId}/close
     * </pre>
     */
    public CloseChangeRequestResp closeMR(CloseChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/changeRequests/{localId}/close";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());
        url = url.replace("{localId}", req.getLocalId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createPost(url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("closeMR codeup hasError, e:", e);
        }
        log.info("closeMR codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("closeMR codeup openApi failed with nothing");
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("closeMR codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return CloseChangeRequestResp.builder()
                .result(jsonObject.getBoolean(CloseChangeRequestResp.Fields.result))
                .build();
    }

    /**
     * GetCompare - 查询代码比较内容
     * <pre>
     *     https://help.aliyun.com/zh/yunxiao/developer-reference/getcompare
     *
     *     /oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/compares
     * </pre>
     */
    public GetCompareResp getCompare(GetCompareReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put(GetCompareReq.Fields.from, req.getFrom());
        formMap.put(GetCompareReq.Fields.to, req.getTo());
        formMap.put(GetCompareReq.Fields.sourceType, req.getSourceType());
        formMap.put(GetCompareReq.Fields.targetType, req.getTargetType());

        String url = "{baseUrl}/oapi/v1/codeup/organizations/{organizationId}/repositories/{repositoryId}/compares";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{organizationId}", req.getOrganizationId());
        url = url.replace("{repositoryId}", req.getRepositoryId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createGet(url)
                    .addHeaders(headers)
                    .form(formMap)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("getCompare codeup hasError, e:", e);
        }
        log.info("getCompare codeup openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("getCompare openApi failed with nothing");
        }

        if (responseBody.contains(ERROR_CODE)) {
            throw new RuntimeException("getCompare codeup openApi failedWithMsg, responseBody:" + responseBody);
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return GetCompareResp.builder()
                .commits(jsonObject.getJSONArray(GetCompareResp.Fields.commits))
                .build();
    }

    /**
     * 获取合并总状态
     * <pre>
     *     codeup
     *     status合并请求状态：UNDER_DEV - 开发中；UNDER_REVIEW - 评审中；TO_BE_MERGED - 待合并；CLOSED - 已关闭；MERGED - 已合并。
     *
     *     1、源分支相对目标分支没有改动--> 已合并
     *     2、"status" in ("MERGED","CLOSED") --> 已合并
     *     3、"status"="TO_BE_MERGED" --> 可以合并
     *     4、other --> 正在校验中
     * </pre>
     */
    private MergeTotalStatusEnum getMergeTotalStatus(String responseBody,
                                                     JSONObject jsonObject) {
        if (StrUtil.isNotEmpty(responseBody)
                && responseBody.contains(NOT_NEED_MERGE_RESPONSE_MSG)) {
            return MergeTotalStatusEnum.MERGED;
        }

        String status = jsonObject.getString("status");
        if (StrUtil.equals("MERGED", status)
                || StrUtil.equals("CLOSED", status)) {
            return MergeTotalStatusEnum.MERGED;
        }

        if (StrUtil.equals("TO_BE_MERGED", status)) {
            return MergeTotalStatusEnum.CAN_BE_MERGED;
        }

        return MergeTotalStatusEnum.CHECKING;
    }

    /**
     * MR是否处于打开状态
     * true代表打开状态
     * false代表已合并或已关闭状态
     */
    private boolean getOpenFlag(JSONObject jsonObject) {
        String status = jsonObject.getString("status");
        if (StrUtil.equals("MERGED", status)
                || StrUtil.equals("CLOSED", status)) {
            return false;
        }

        return true;
    }

}