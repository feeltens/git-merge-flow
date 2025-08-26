package com.feeltens.git.service;

import com.feeltens.git.vo.base.CloudResponse;
import com.feeltens.git.vo.base.PageRequest;
import com.feeltens.git.vo.base.PageResponse;
import com.feeltens.git.vo.req.AddGitOrganizationReqVO;
import com.feeltens.git.vo.req.AddGitProjectReqVO;
import com.feeltens.git.vo.req.AddIntoMixBranchReqVO;
import com.feeltens.git.vo.req.CreateGitBranchReqVO;
import com.feeltens.git.vo.req.DeleteGitBranchReqVO;
import com.feeltens.git.vo.req.PageGitBranchReqVO;
import com.feeltens.git.vo.req.PageGitOrganizationReqVO;
import com.feeltens.git.vo.req.PageGitProjectReqVO;
import com.feeltens.git.vo.req.PageMixBranchReqVO;
import com.feeltens.git.vo.req.QueryMixBranchReqVO;
import com.feeltens.git.vo.req.RemergeMixBranchReqVO;
import com.feeltens.git.vo.req.RemoveFromMixBranchReqVO;
import com.feeltens.git.vo.resp.AddIntoMixBranchRespVO;
import com.feeltens.git.vo.resp.CreateGitBranchRespVO;
import com.feeltens.git.vo.resp.DeleteGitBranchRespVO;
import com.feeltens.git.vo.resp.ListOrganizationsRespVO;
import com.feeltens.git.vo.resp.PageGitBranchRespVO;
import com.feeltens.git.vo.resp.PageGitOrganizationRespVO;
import com.feeltens.git.vo.resp.PageGitProjectRespVO;
import com.feeltens.git.vo.resp.PageMixBranchRespVO;
import com.feeltens.git.vo.resp.QueryMixBranchRespVO;
import com.feeltens.git.vo.resp.RemergeMixBranchRespVO;
import com.feeltens.git.vo.resp.RemoveFromMixBranchRespVO;

import java.util.List;

/**
 * git flow 服务接口
 *
 * @author feeltens
 * @date 2025-08-19
 */
public interface GitFlowService {

    /**
     * 查询组织列表
     */
    CloudResponse<List<ListOrganizationsRespVO>> listOrganizations();

    /**
     * 分页查询git组织
     */
    PageResponse<PageGitOrganizationRespVO> pageGitOrganization(PageRequest<PageGitOrganizationReqVO> req);

    /**
     * 列表查询git组织
     */
    List<PageGitOrganizationRespVO> listGitOrganization();

    /**
     * 添加git组织
     */
    CloudResponse<String> addGitOrganization(AddGitOrganizationReqVO req);

    /**
     * 添加git工程
     */
    CloudResponse<String> addGitProject(AddGitProjectReqVO req);

    /**
     * 创建git原始分支
     */
    CloudResponse<CreateGitBranchRespVO> createGitBranch(CreateGitBranchReqVO req);

    /**
     * 分页查询git工程
     */
    PageResponse<PageGitProjectRespVO> pageGitProject(PageRequest<PageGitProjectReqVO> req);

    /**
     * 分页查询git中间分支
     */
    PageResponse<PageMixBranchRespVO> pageMixBranch(PageRequest<PageMixBranchReqVO> req);

    /**
     * 分页查询git原始分支
     */
    PageResponse<PageGitBranchRespVO> pageGitBranch(PageRequest<PageGitBranchReqVO> req);

    /**
     * 查询git中间分支详情
     */
    QueryMixBranchRespVO queryMixBranch(QueryMixBranchReqVO req);

    /**
     * 重新合并git中间分支
     * <pre>
     *     1、当前集成分支是否已合并进主分支？已进主分支，则不再参与合并。
     *     2、合并主分支到中间分支
     *     3、合并已集成的分支到中间分支
     * </pre>
     */
    RemergeMixBranchRespVO remergeMixBranch(RemergeMixBranchReqVO req);

    /**
     * 添加分支进git中间分支
     */
    AddIntoMixBranchRespVO addIntoMixBranch(AddIntoMixBranchReqVO req);

    /**
     * 从git中间分支移除源分支
     */
    RemoveFromMixBranchRespVO removeFromMixBranch(RemoveFromMixBranchReqVO req);

    /**
     * 删除git分支
     */
    DeleteGitBranchRespVO deleteGitBranch(DeleteGitBranchReqVO req);

}