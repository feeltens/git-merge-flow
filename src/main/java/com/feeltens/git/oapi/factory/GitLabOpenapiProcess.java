package com.feeltens.git.oapi.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
 * GitLab Oapi 工具类
 */
@Service
@Slf4j
public class GitLabOpenapiProcess implements GitOpenApiProcess {

    private static final String HEADER_TOKEN_KEY = "PRIVATE-TOKEN";
    private static final String ALREADY_HAS_MERGE_RESPONSE_MSG = "此源分支已存在另一个开放的合并请求：!";

    /**
     * 获取git服务平台枚举
     */
    @Override
    public GitServiceEnum getGitServiceEnum() {
        return GitServiceEnum.GITLAB;
    }

    /**
     * ListOrganizations - 查询组织列表
     * <pre>
     *     not found api
     * </pre>
     */
    public ListOrganizationsResp listOrganizations(ListOrganizationsReq req) {
        // mock
        ListOrganizationsResp.ListOrganizationsRespItem repoItem = ListOrganizationsResp.ListOrganizationsRespItem.builder()
                .id("default")
                .name("default")
                .description("")
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        return new ListOrganizationsResp(Lists.newArrayList(repoItem));
    }

    /**
     * ListRepositories - 查询代码库列表
     * <pre>
     *     https://docs.gitlab.com/18.3/api/projects/#list-projects
     *
     *      /api/v4/projects
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

    public ListRepositoriesResp doListRepositories(ListRepositoriesReq req, int page) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        if (StrUtil.isNotEmpty(req.getSearch())) {
            formMap.put(ListRepositoriesReq.Fields.search, req.getSearch());
        }
        formMap.put("page", page);
        formMap.put("per_page", 100);
        formMap.put("order_by", "id");
        formMap.put("sort", "asc");

        String url = "{baseUrl}/api/v4/projects";
        url = url.replace("{baseUrl}", req.getBaseUrl());

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
            log.error("listRepositories gitlab hasError, e:", e);
        }
        log.info("listRepositories gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("listRepositories gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new ListRepositoriesResp();
        }

        List<JSONObject> list = JSON.parseArray(responseBody, JSONObject.class);
        List<ListRepositoriesResp.ListRepositoriesRespItem> repoItemList = Lists.newArrayList();
        for (JSONObject jsonObject : list) {
            ListRepositoriesResp.ListRepositoriesRespItem repoItem = ListRepositoriesResp.ListRepositoriesRespItem.builder()
                    .id(jsonObject.getLong(ListRepositoriesResp.ListRepositoriesRespItem.Fields.id))
                    .name(jsonObject.getString(ListRepositoriesResp.ListRepositoriesRespItem.Fields.name))
                    .description(jsonObject.getString(ListRepositoriesResp.ListRepositoriesRespItem.Fields.description))
                    .createdAt(DateTimeXUtil.isoString2Date(jsonObject.getString("created_at")))
                    .updatedAt(DateTimeXUtil.isoString2Date(jsonObject.getString("updated_at")))
                    .build();
            repoItemList.add(repoItem);
        }
        return new ListRepositoriesResp(repoItemList);
    }

    /**
     * GetRepository - 查询代码库
     * <pre>
     *     https://docs.gitlab.com/18.3/api/projects/#get-a-single-project
     *
     *      /api/v4/projects/{id}
     * </pre>
     */
    public GetRepositoryResp getRepository(GetRepositoryReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/api/v4/projects/{id}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());

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
            log.error("getRepository gitlab hasError, e:", e);
        }
        log.info("getRepository gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("getRepository gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new GetRepositoryResp();
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return GetRepositoryResp.builder()
                .id(jsonObject.getLong(GetRepositoryResp.Fields.id))
                .name(jsonObject.getString(GetRepositoryResp.Fields.name))
                .defaultBranch(jsonObject.getString("default_branch"))
                .createdAt(DateTimeXUtil.isoString2Date(jsonObject.getString("created_at")))
                .updatedAt(DateTimeXUtil.isoString2Date(jsonObject.getString("updated_at")))
                .httpUrlToRepo(jsonObject.getString("http_url_to_repo"))
                .sshUrlToRepo(jsonObject.getString("ssh_url_to_repo"))
                .webUrl(jsonObject.getString("web_url"))
                .build();
    }

    /**
     * CreateBranch - 创建分支
     * <pre>
     *      https://docs.gitlab.com/18.3/api/branches/#create-repository-branch
     *
     *      /api/v4/projects/{id}/repository/branches
     * </pre>
     */
    public CreateBranchResp createBranch(CreateBranchReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put(CreateBranchReq.Fields.branch, req.getBranch());
        formMap.put(CreateBranchReq.Fields.ref, req.getRef());

        String url = "{baseUrl}/api/v4/projects/{id}/repository/branches";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());

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
            log.error("createBranch gitlab hasError, e:", e);
        }
        log.info("createBranch gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        // if (status != 200) {
        //     throw new RuntimeException("createBranch gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        // }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("createBranch gitlab openApi failed with nothing");
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        JSONObject commitJsonObj = jsonObject.getObject(CreateBranchResp.Fields.commit, JSONObject.class);
        return CreateBranchResp.builder()
                .name(jsonObject.getString(CreateBranchResp.Fields.name))
                .authorName(commitJsonObj.getString("author_name"))
                .authorEmail(commitJsonObj.getString("author_email"))
                .committedDate(DateTimeXUtil.isoString2Date(commitJsonObj.getString("committed_date")))
                .webUrl(jsonObject.getString(CreateBranchResp.Fields.webUrl))
                .lastCommitTime(DateTimeXUtil.isoString2Date(commitJsonObj.getString("committed_date")))
                .lastCommitUser(commitJsonObj.getString("committer_name"))
                .lastCommitEmail(commitJsonObj.getString("committer_email"))
                .lastCommitId(commitJsonObj.getString("id"))
                .lastCommitShortId(commitJsonObj.getString("short_id"))
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
     *     https://docs.gitlab.com/18.3/api/branches/#get-single-repository-branch
     *
     *      /api/v4/projects/{id}/repository/branches/{branch}
     * </pre>
     */
    public GetBranchResp getBranch(GetBranchReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/api/v4/projects/{id}/repository/branches/{branch}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());
        url = url.replace("{branch}", getEncodeStr(req.getBranchName()));

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
            log.error("getBranch gitlab hasError, e:", e);
        }
        log.info("getBranch gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("getBranch gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("getBranch gitlab openApi failed with nothing");
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        JSONObject commitJsonObj = jsonObject.getObject(GetBranchResp.Fields.commit, JSONObject.class);
        return GetBranchResp.builder()
                .name(jsonObject.getString(GetBranchResp.Fields.name))
                .authorName(commitJsonObj.getString("author_name"))
                .authorEmail(commitJsonObj.getString("author_email"))
                .committedDate(DateTimeXUtil.isoString2Date(commitJsonObj.getString("committed_date")))
                .webUrl(jsonObject.getString("web_url"))
                .build();
    }

    /**
     * ListBranches - 查询分支列表
     * <pre>
     *     https://docs.gitlab.com/18.3/api/branches/#list-repository-branches
     *
     *      /api/v4/projects/{id}/repository/branches
     * </pre>
     */
    public ListBranchesResp listBranches(ListBranchesReq req) {
        int page = 1;
        List<ListBranchesResp.BranchItem> resultList = Lists.newArrayList();
        // 最大100w调用，防止死循环
        for (int i = 0; i < 100_0000; i++) {
            ListBranchesResp resp = doListBranches(req, page);
            if (null != resp && CollUtil.isNotEmpty(resp.getBranchItemList())) {
                CollUtil.addAll(resultList, resp.getBranchItemList());
                page++;
            } else {
                break;
            }
        }
        return new ListBranchesResp(resultList);
    }

    public ListBranchesResp doListBranches(ListBranchesReq req, int page) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put("page", page);
        formMap.put("per_page", 100);
        formMap.put("order_by", "id");
        formMap.put("sort", "asc");

        String url = "{baseUrl}/api/v4/projects/{id}/repository/branches";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());

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
            log.error("listBranches gitlab hasError, e:", e);
        }
        log.info("listBranches gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("listBranches gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new ListBranchesResp();
        }

        List<JSONObject> list = JSON.parseArray(responseBody, JSONObject.class);
        List<ListBranchesResp.BranchItem> branchItemList = Lists.newArrayList();
        for (JSONObject jsonObject : list) {
            JSONObject commitJsonObj = jsonObject.getObject(ListBranchesResp.BranchItem.Fields.commit, JSONObject.class);
            ListBranchesResp.BranchItem branchItem = ListBranchesResp.BranchItem.builder()
                    .name(jsonObject.getString(ListBranchesResp.BranchItem.Fields.name))
                    .authorName(commitJsonObj.getString("author_name"))
                    .authorEmail(commitJsonObj.getString("author_email"))
                    .committedDate(DateTimeXUtil.isoString2Date(commitJsonObj.getString("committed_date")))
                    .webUrl(jsonObject.getString("web_url"))
                    .defaultBranch(jsonObject.getBoolean("default"))
                    .lastCommitTime(DateTimeXUtil.isoString2Date(commitJsonObj.getString("committed_date")))
                    .lastCommitUser(commitJsonObj.getString("author_name"))
                    .lastCommitEmail(commitJsonObj.getString("author_email"))
                    .lastCommitId(commitJsonObj.getString("id"))
                    .lastCommitShortId(commitJsonObj.getString("short_id"))
                    .lastCommitMessage(commitJsonObj.getString("message"))
                    .build();
            branchItemList.add(branchItem);
        }
        return new ListBranchesResp(branchItemList);
    }

    /**
     * DeleteBranch - 删除分支
     * <pre>
     *     https://docs.gitlab.com/18.3/api/branches/#delete-repository-branch
     *
     *      /api/v4/projects/{id}/repository/branches/{branch}
     * </pre>
     */
    public DeleteBranchResp deleteBranch(DeleteBranchReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/api/v4/projects/{id}/repository/branches/{branch}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());
        url = url.replace("{branch}", getEncodeStr(req.getBranchName()));

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
            log.error("deleteBranch gitlab hasError, e:", e);
        }
        log.info("deleteBranch gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        // if (status != 200) {
        //     throw new RuntimeException("deleteBranch gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        // }

        if (status != 204) {
            return new DeleteBranchResp();
        }
        // http status = 204，代表删除成功
        return new DeleteBranchResp(req.getBranchName());
    }

    /**
     * CreateChangeRequest - 创建合并请求
     * <pre>
     *     https://docs.gitlab.com/18.3/api/merge_requests/#create-mr
     *
     *      /api/v4/projects/{id}/merge_requests
     * </pre>
     */
    public CreateChangeRequestResp createChangeRequest(CreateChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String title = String.format("GitMergeFlow: merge branch %s into %s", req.getSourceBranch(), req.getTargetBranch());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put("source_branch", req.getSourceBranch());
        formMap.put("target_branch", req.getTargetBranch());
        formMap.put("title", title);
        formMap.put("description", title);

        String url = "{baseUrl}/api/v4/projects/{id}/merge_requests";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());

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
            log.error("createChangeRequest gitlab hasError, e:", e);
        }
        log.info("createChangeRequest gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        // if (status != 200) {
        //     throw new RuntimeException("createChangeRequest gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        // }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("createChangeRequest gitlab openApi failed with nothing");
        }

        // responseBody: {"message":["此源分支已存在另一个开放的合并请求：!7"]}
        // status:409
        if (status == 409 && responseBody.contains(ALREADY_HAS_MERGE_RESPONSE_MSG)) {
            JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
            JSONArray messageArr = jsonObject.getJSONArray("message");
            String msg = String.valueOf(messageArr.get(0));
            String[] split = msg.split(ALREADY_HAS_MERGE_RESPONSE_MSG);
            Long iid = Long.valueOf(split[1]);
            return CreateChangeRequestResp.builder()
                    .localId(iid)
                    .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                    .projectId(req.getRepositoryId())
                    .sourceBranch(req.getSourceBranch())
                    .targetBranch(req.getTargetBranch())
                    .title(jsonObject.getString(CreateChangeRequestResp.Fields.title))
                    .description(jsonObject.getString(CreateChangeRequestResp.Fields.description))
                    .createTime(null)
                    .updateTime(null)
                    .build();
        }

        /*
        "id": 1,
        "iid": 1,
        "project_id": 1,
        "state": "opened",
        "has_conflicts": false,
        "merge_status": "checking",
        */

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        String createTimeStr = jsonObject.getString("created_at");
        String updateTimeStr = jsonObject.getString("updated_at");
        Date createTime = StrUtil.isNotEmpty(createTimeStr) ? DateTimeXUtil.isoString2Date(createTimeStr) : null;
        Date updateTime = StrUtil.isNotEmpty(updateTimeStr) ? DateTimeXUtil.isoString2Date(updateTimeStr) : null;
        return CreateChangeRequestResp.builder()
                .localId(jsonObject.getLong("iid"))
                .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                .projectId(jsonObject.getLong("project_id"))
                .sourceBranch(jsonObject.getString("source_branch"))
                .targetBranch(jsonObject.getString("target_branch"))
                .title(jsonObject.getString(CreateChangeRequestResp.Fields.title))
                .description(jsonObject.getString(CreateChangeRequestResp.Fields.description))
                .createTime(createTime)
                .updateTime(updateTime)
                .build();
    }

    /**
     * ListChangeRequests - 查询开启状态的合并请求列表
     */
    // public ListChangeRequestsResp listOpenedRequests(ListChangeRequestsReq req) {
    //     req.setState("opened");
    //     return listChangeRequests(req);
    // }

    /**
     * <pre>
     *     https://docs.gitlab.com/18.3/api/merge_requests/#list-merge-requests
     *
     *     /api/v4/merge_requests
     * </pre>
     */
    /*public ListChangeRequestsResp listChangeRequests(ListChangeRequestsReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put("iids", req.getProjectIds().split(","));
        formMap.put("state", req.getState());

        String url = "{baseUrl}/api/v4/merge_requests";
        url = url.replace("{baseUrl}", req.getBaseUrl());

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
            log.error("listChangeRequests gitlab hasError, e:", e);
        }
        log.info("listChangeRequests gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("listChangeRequests gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("listChangeRequests gitlab openApi failed with nothing");
        }

        List<JSONObject> jsonObjectList = JSON.parseArray(responseBody, JSONObject.class);
        List<ListChangeRequestsResp.ListChangeRequestsItem> requestItemList = Lists.newArrayList();
        for (JSONObject jsonObject : jsonObjectList) {
            ListChangeRequestsResp.ListChangeRequestsItem requestItem = ListChangeRequestsResp.ListChangeRequestsItem.builder()
                    .localId(jsonObject.getLong("iid"))
                    // .hasConflict(jsonObject.getBoolean("has_conflicts"))
                    // .state(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.state))
                    .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                    .projectId(jsonObject.getLong("project_id"))
                    .sourceBranch(jsonObject.getString("source_branch"))
                    .targetBranch(jsonObject.getString("target_branch"))
                    .title(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.title))
                    .description(jsonObject.getString(ListChangeRequestsResp.ListChangeRequestsItem.Fields.description))
                    .createdAt(DateTimeXUtil.isoString2Date(jsonObject.getString("created_at")))
                    .updatedAt(DateTimeXUtil.isoString2Date(jsonObject.getString("updated_at")))
                    .build();
            requestItemList.add(requestItem);
        }

        return new ListChangeRequestsResp(requestItemList);
    }*/

    /**
     * ListMergedRequests - 查询已合并状态的合并请求列表
     */
    // public ListChangeRequestsResp listMergedRequests(ListChangeRequestsReq req) {
    //     req.setState("merged");
    //     return listChangeRequests(req);
    // }

    /**
     * GetChangeRequest - 查询合并请求
     * <pre>
     *     https://docs.gitlab.com/18.3/api/merge_requests/#get-single-mr
     *
     *      /api/v4/projects/{id}/merge_requests/{merge_request_iid}
     * </pre>
     */
    public GetChangeRequestResp getChangeRequest(GetChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        String url = "{baseUrl}/api/v4/projects/{id}/merge_requests/{merge_request_iid}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());
        url = url.replace("{merge_request_iid}", req.getLocalId().toString());

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
            log.error("getChangeRequest gitlab hasError, e:", e);
        }
        log.info("getChangeRequest gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("getChangeRequest gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            return new GetChangeRequestResp();
        }

        /*
        id : ID of the merge request.
        iid : Internal ID of the merge request.
        "state": "opened", opened, closed, merged or locked
        "has_conflicts": false,
        merge_status : unchecked, checking, can_be_merged, cannot_be_merged, or cannot_be_merged_recheck
        */

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return GetChangeRequestResp.builder()
                .localId(jsonObject.getLong("iid"))
                .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                .openFlag(getOpenFlag(jsonObject))
                .projectId(jsonObject.getLong("project_id"))
                .sourceBranch(jsonObject.getString("source_branch"))
                .targetBranch(jsonObject.getString("target_branch"))
                .title(jsonObject.getString(GetChangeRequestResp.Fields.title))
                .description(jsonObject.getString(GetChangeRequestResp.Fields.description))
                .createTime(DateTimeXUtil.isoString2Date(jsonObject.getString("created_at")))
                .updateTime(DateTimeXUtil.isoString2Date(jsonObject.getString("updated_at")))
                .diffRefsObj(jsonObject.getJSONObject("diff_refs"))
                .build();
    }

    /**
     * MergeChangeRequest - 合并合并请求
     * <pre>
     *      https://docs.gitlab.com/18.3/api/merge_requests/#merge-a-merge-request
     *
     *      /api/v4/projects/{id}/merge_requests/{merge_request_iid}/merge
     * </pre>
     */
    public MergeChangeRequestResp mergeChangeRequest(MergeChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put("auto_merge", "true");
        formMap.put("merge_commit_message", "merge by GitMergeFlow");

        String url = "{baseUrl}/api/v4/projects/{id}/merge_requests/{merge_request_iid}/merge";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());
        url = url.replace("{merge_request_iid}", req.getLocalId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createRequest(Method.PUT, url)
                    .addHeaders(headers)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("mergeChangeRequest gitlab hasError, e:", e);
        }
        log.info("mergeChangeRequest gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        // if (status != 200) {
        //     throw new RuntimeException("mergeChangeRequest gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        // }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("mergeChangeRequest gitlab openApi failed with nothing");
        }

        // status=405
        // message: 405 Method Not Allowed
        // reason: The merge request cannot merge.

        // status=422
        // message: Branch cannot be merged
        // reason: The merge request failed to merge.

        if (status == 405 || status == 422) {
            // 比较合并请求的分支差异
            Boolean hasDiffsFlag = mergeRequestHasDiffs(req);
            if (null != hasDiffsFlag && !hasDiffsFlag) {
                // 没有文件差异，则关闭MR
                CloseChangeRequestReq closeReq = new CloseChangeRequestReq();
                closeReq.setBaseUrl(req.getBaseUrl());
                closeReq.setAccessToken(req.getAccessToken());
                closeReq.setOrganizationId(req.getOrganizationId());
                closeReq.setRepositoryId(req.getRepositoryId());
                closeReq.setLocalId(req.getLocalId());
                CloseChangeRequestResp closeChangeRequestResp = closeMR(closeReq);
                if (closeChangeRequestResp.getResult()) {
                    return MergeChangeRequestResp.builder()
                            .localId(req.getLocalId())
                            .mergeTotalStatus(MergeTotalStatusEnum.MERGED.getStatus()) // 已关闭，此外理解为【已合并】
                            .projectId(req.getRepositoryId())
                            .build();
                }
            }

            return MergeChangeRequestResp.builder()
                    .localId(req.getLocalId())
                    .mergeTotalStatus(MergeTotalStatusEnum.CAN_NOT_BE_MERGED.getStatus()) // 无法合并
                    .projectId(req.getRepositoryId())
                    .build();
        }

        /*
        "merge_status": "can_be_merged",
        "has_conflicts": false,
        "state": "merged"
        */

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return MergeChangeRequestResp.builder()
                .localId(jsonObject.getLong("iid"))
                .mergeTotalStatus(getMergeTotalStatus(responseBody, jsonObject).getStatus())
                .projectId(jsonObject.getLong("project_id"))
                .sourceBranch(jsonObject.getString("source_branch"))
                .targetBranch(jsonObject.getString("target_branch"))
                .title(jsonObject.getString(MergeChangeRequestResp.Fields.title))
                .description(jsonObject.getString(MergeChangeRequestResp.Fields.description))
                .createTime(DateTimeXUtil.isoString2Date(jsonObject.getString("created_at")))
                .updateTime(DateTimeXUtil.isoString2Date(jsonObject.getString("updated_at")))
                .build();
    }

    /**
     * 比较两个分支之间的差异
     * <pre>
     *     虽然has_conflicts=true，但是base_sha、head_sha、start_sha都相同，则认为没有差异
     *
     *      "has_conflicts": true,
     *
     *     "diff_refs": {
     *         "base_sha": "d04a0e406ac012d4e52013c5e0e088907de2f08b",
     *         "head_sha": "1be4e821f51a88666acf3e12d2744b724f5b9df4",
     *         "start_sha": "936f98b713652e09e46b922f8cbfff955c9dcb31"
     *     },
     *
     * </pre>
     *
     * @param req
     * @return true代表有差异；false代表没有差异；null代表无法判断
     */
    private Boolean mergeRequestHasDiffs(MergeChangeRequestReq req) {
        GetChangeRequestReq getChangeRequest = new GetChangeRequestReq();
        getChangeRequest.setBaseUrl(req.getBaseUrl());
        getChangeRequest.setAccessToken(req.getAccessToken());
        getChangeRequest.setOrganizationId(req.getOrganizationId());
        getChangeRequest.setRepositoryId(req.getRepositoryId());
        getChangeRequest.setLocalId(req.getLocalId());

        GetChangeRequestResp getChangeRequestResp = getChangeRequest(getChangeRequest);

        if (null == getChangeRequestResp || null == getChangeRequestResp.getDiffRefsObj()) {
            // 无法判断
            return null;
        }

        JSONObject diffRefsObj = getChangeRequestResp.getDiffRefsObj();
        String baseShaStr = diffRefsObj.getString("base_sha"); // 创建 MR 时目标分支的最新提交
        String headShaStr = diffRefsObj.getString("head_sha"); // 源分支的当前最新提交
        if (StrUtil.isNotEmpty(baseShaStr) && StrUtil.equals(baseShaStr, headShaStr)) {
            // 没有差异
            return false;
        }

        return null;
    }

    /**
     * Update MR - 修改合并请求
     * <pre>
     *      https://docs.gitlab.com/18.3/api/merge_requests/#update-mr
     *
     *      /api/v4/projects/{id}/merge_requests/{merge_request_iid}
     * </pre>
     */
    public CloseChangeRequestResp closeMR(CloseChangeRequestReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put("description", "GitMergeFlow: closeMR");
        formMap.put("state_event", "close");

        String url = "{baseUrl}/api/v4/projects/{id}/merge_requests/{merge_request_iid}";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());
        url = url.replace("{merge_request_iid}", req.getLocalId().toString());

        long start = System.currentTimeMillis();
        String responseBody = "";
        int status = 0;
        try {
            HttpResponse execute = HttpUtil.createRequest(Method.PUT, url)
                    .addHeaders(headers)
                    .form(formMap)
                    .execute();
            status = execute.getStatus();
            responseBody = execute.body();
        } catch (Exception e) {
            log.error("closeMR gitlab hasError, e:", e);
        }
        log.info("closeMR gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        // if (status != 200) {
        //     throw new RuntimeException("mergeChangeRequest gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        // }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("closeMR gitlab openApi failed with nothing");
        }

        /*
        "state": "closed",
        */
        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        // return jsonObject.getString("state").equals("closed");
        return new CloseChangeRequestResp(jsonObject.getString("state").equals("closed"));
    }

    /**
     * GetCompare - 查询代码比较内容
     * <pre>
     *      https://docs.gitlab.com/18.3/api/repositories/#compare-branches-tags-or-commits
     *
     *     /api/v4/projects/{id}/repository/compare
     * </pre>
     */
    public GetCompareResp getCompare(GetCompareReq req) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_TOKEN_KEY, req.getAccessToken());

        Map<String, Object> formMap = new HashMap<>();
        formMap.put(GetCompareReq.Fields.from, req.getFrom());
        formMap.put(GetCompareReq.Fields.to, req.getTo());

        String url = "{baseUrl}/api/v4/projects/{id}/repository/compare";
        url = url.replace("{baseUrl}", req.getBaseUrl());
        url = url.replace("{id}", req.getRepositoryId().toString());

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
            log.error("getCompare gitlab hasError, e:", e);
        }
        log.info("getCompare gitlab openApi hasResult:{}    status:{}    param:{}    costTime:{}ms",
                responseBody, status, JSON.toJSONString(req), System.currentTimeMillis() - start);

        if (status != 200) {
            throw new RuntimeException("getCompare gitlab openApi failedWithFailStatus, status:" + status + " responseBody:" + responseBody);
        }

        if (StrUtil.isEmptyIfStr(responseBody)) {
            throw new RuntimeException("getCompare gitlab openApi failed with nothing");
        }

        JSONObject jsonObject = JSON.parseObject(responseBody, JSONObject.class);
        return GetCompareResp.builder()
                .commits(jsonObject.getJSONArray(GetCompareResp.Fields.commits))
                .build();
    }

    /**
     * 获取合并总状态
     * <pre>
     *     gitlab
     *
     *     1、此源分支已存在另一个开放的合并请求 --> 可以合并
     *     2、"state" in ("merged", "closed")  --> 已合并
     *     3、diff_refs 里的 base_sha = head_sha --> 分支没有差异，可以合并
     *     4、merge_status = can_be_merged --> 可以合并
     *     5、merge_status in (unchecked, checking) --> 正在校验中
     *     6、other --> 无法合并
     * </pre>
     */
    private MergeTotalStatusEnum getMergeTotalStatus(String responseBody,
                                                     JSONObject jsonObject) {
        if (StrUtil.isNotEmpty(responseBody)
                && responseBody.contains(ALREADY_HAS_MERGE_RESPONSE_MSG)) {
            return MergeTotalStatusEnum.CAN_BE_MERGED;
        }

        String state = jsonObject.getString("state");
        if (StrUtil.equals("merged", state)
                || StrUtil.equals("closed", state)) {
            return MergeTotalStatusEnum.MERGED;
        }

        JSONObject diffRefsObj = jsonObject.getJSONObject("diff_refs");
        if (null != diffRefsObj) {
            String baseShaStr = diffRefsObj.getString("base_sha");
            String headShaStr = diffRefsObj.getString("head_sha");
            if (StrUtil.isNotEmpty(baseShaStr) && StrUtil.equals(baseShaStr, headShaStr)) {
                // 分支之间没有差异，则可以合并
                return MergeTotalStatusEnum.CAN_BE_MERGED;
            }
        }

        String mergeStatus = jsonObject.getString("merge_status");
        // 可以合并
        if (StrUtil.equals("can_be_merged", mergeStatus)) {
            return MergeTotalStatusEnum.CAN_BE_MERGED;
        }
        // 正在校验中
        if (StrUtil.equals("unchecked", mergeStatus)
                || StrUtil.equals("checking", mergeStatus)) {
            return MergeTotalStatusEnum.CHECKING;
        }
        // 无法合并
        return MergeTotalStatusEnum.CAN_NOT_BE_MERGED;
    }

    /**
     * MR是否处于打开状态
     * true代表打开状态
     * false代表已合并或已关闭状态
     */
    private boolean getOpenFlag(JSONObject jsonObject) {
        String state = jsonObject.getString("state");
        if (StrUtil.equals("merged", state)
                || StrUtil.equals("closed", state)) {
            return false;
        }

        return true;
    }

}