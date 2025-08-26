package com.feeltens.git.oapi.factory;

import com.feeltens.git.enums.GitServiceEnum;
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

public interface GitOpenApiProcess {

    /**
     * 获取git服务平台枚举
     */
    GitServiceEnum getGitServiceEnum();

    /**
     * ListOrganizations - 查询组织列表
     */
    ListOrganizationsResp listOrganizations(ListOrganizationsReq req);

    /**
     * ListRepositories - 查询代码库列表
     */
    ListRepositoriesResp listRepositories(ListRepositoriesReq req);

    /**
     * GetRepository - 查询代码库
     */
    GetRepositoryResp getRepository(GetRepositoryReq req);

    /**
     * CreateBranch - 创建分支
     */
    CreateBranchResp createBranch(CreateBranchReq req);

    /**
     * GetBranch - 查询分支信息
     */
    GetBranchResp getBranch(GetBranchReq req);

    /**
     * ListBranches - 查询分支列表
     */
    ListBranchesResp listBranches(ListBranchesReq req);

    /**
     * DeleteBranch - 删除分支
     */
    DeleteBranchResp deleteBranch(DeleteBranchReq req);

    /**
     * CreateChangeRequest - 创建合并请求
     */
    CreateChangeRequestResp createChangeRequest(CreateChangeRequestReq req);

    /**
     * ListChangeRequests - 查询合并请求列表
     */
    // ListChangeRequestsResp listChangeRequests(ListChangeRequestsReq req);

    /**
     * GetChangeRequest - 查询合并请求
     */
    GetChangeRequestResp getChangeRequest(GetChangeRequestReq req);

    /**
     * MergeChangeRequest - 合并合并请求
     */
    MergeChangeRequestResp mergeChangeRequest(MergeChangeRequestReq req);

    /**
     * GetCompare - 查询代码比较内容
     */
    GetCompareResp getCompare(GetCompareReq req);

}