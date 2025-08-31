package com.feeltens.git.oapi.factory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.feeltens.git.config.GitMergeFlowConfig;
import com.feeltens.git.enums.GitServiceEnum;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * git open api 工厂组件
 *
 * @author feeltens
 * @date 2025-08-24
 */
@Component
public class GitOpenApiFactory {

    @Resource
    private GitMergeFlowConfig gitMergeFlowConfig;
    @Resource
    private List<GitOpenApiProcess> processList;

    private Map<GitServiceEnum, GitOpenApiProcess> processMap = new HashMap<>();

    @PostConstruct
    private void initProcessMap() {
        if (CollUtil.isEmpty(processList)) {
            return;
        }

        for (GitOpenApiProcess process : processList) {
            processMap.put(process.getGitServiceEnum(), process);
        }
    }

    private GitOpenApiProcess getProcess() {
        GitServiceEnum gitServiceEnum = GitServiceEnum.getByEnvCode(gitMergeFlowConfig.getGitService());
        GitOpenApiProcess process = this.processMap.get(gitServiceEnum);
        Assert.isTrue(null != process, "未找到对应处理策略gitServiceEnum=" + gitServiceEnum);
        return process;
    }

    /**
     * ListOrganizations - 查询组织列表
     */
    public ListOrganizationsResp listOrganizations(ListOrganizationsReq req) {
        return getProcess().listOrganizations(req);
    }

    /**
     * ListRepositories - 查询代码库列表
     */
    public ListRepositoriesResp listRepositories(ListRepositoriesReq req) {
        return getProcess().listRepositories(req);
    }

    /**
     * GetRepository - 查询代码库
     */
    public GetRepositoryResp getRepository(GetRepositoryReq req) {
        return getProcess().getRepository(req);
    }

    /**
     * CreateBranch - 创建分支
     */
    public CreateBranchResp createBranch(CreateBranchReq req) {
        return getProcess().createBranch(req);
    }

    /**
     * GetBranch - 查询分支信息
     */
    public GetBranchResp getBranch(GetBranchReq req) {
        return getProcess().getBranch(req);
    }

    /**
     * ListBranches - 查询分支列表
     */
    public ListBranchesResp listBranches(ListBranchesReq req) {
        return getProcess().listBranches(req);
    }

    /**
     * DeleteBranch - 删除分支
     */
    public DeleteBranchResp deleteBranch(DeleteBranchReq req) {
        return getProcess().deleteBranch(req);
    }

    /**
     * CreateChangeRequest - 创建合并请求
     */
    public CreateChangeRequestResp createChangeRequest(CreateChangeRequestReq req) {
        return getProcess().createChangeRequest(req);
    }

    /**
     * ListChangeRequests - 查询合并请求列表
     */
    // public ListChangeRequestsResp listChangeRequests(ListChangeRequestsReq req) {
    //     return getProcess().listChangeRequests(req);
    // }

    /**
     * GetChangeRequest - 查询合并请求
     */
    public GetChangeRequestResp getChangeRequest(GetChangeRequestReq req) {
        return getProcess().getChangeRequest(req);
    }

    /**
     * MergeChangeRequest - 合并合并请求
     */
    public MergeChangeRequestResp mergeChangeRequest(MergeChangeRequestReq req) {
        return getProcess().mergeChangeRequest(req);
    }

    /**
     * closeMR - 关闭合并请求
     */
    public CloseChangeRequestResp closeMR(CloseChangeRequestReq req) {
        return getProcess().closeMR(req);
    }

    /**
     * GetCompare - 查询代码比较内容
     */
    public GetCompareResp getCompare(GetCompareReq req) {
        return getProcess().getCompare(req);
    }

}