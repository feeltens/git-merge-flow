package com.feeltens.git.controller;

import com.alibaba.fastjson.JSON;
import com.feeltens.git.service.GitFlowService;
import com.feeltens.git.vo.base.CloudResponse;
import com.feeltens.git.vo.base.PageRequest;
import com.feeltens.git.vo.base.PageResponse;
import com.feeltens.git.vo.req.AddGitOrganizationReqVO;
import com.feeltens.git.vo.req.AddGitProjectReqVO;
import com.feeltens.git.vo.req.AddIntoMixBranchReqVO;
import com.feeltens.git.vo.req.CreateGitBranchReqVO;
import com.feeltens.git.vo.req.DeleteGitBranchReqVO;
import com.feeltens.git.vo.req.ListGitBranchReqVO;
import com.feeltens.git.vo.req.ListGitRepositoryReqVO;
import com.feeltens.git.vo.req.PageGitBranchReqVO;
import com.feeltens.git.vo.req.PageGitOrganizationReqVO;
import com.feeltens.git.vo.req.PageGitProjectReqVO;
import com.feeltens.git.vo.req.PageMixBranchReqVO;
import com.feeltens.git.vo.req.PullRemoteBranchReqVO;
import com.feeltens.git.vo.req.QueryGitBranchReqVO;
import com.feeltens.git.vo.req.QueryMixBranchReqVO;
import com.feeltens.git.vo.req.RemergeMixBranchReqVO;
import com.feeltens.git.vo.req.RemoveFromMixBranchReqVO;
import com.feeltens.git.vo.req.UpdateGitBranchReqVO;
import com.feeltens.git.vo.resp.AddIntoMixBranchRespVO;
import com.feeltens.git.vo.resp.CreateGitBranchRespVO;
import com.feeltens.git.vo.resp.DeleteGitBranchRespVO;
import com.feeltens.git.vo.resp.ListGitBranchRespVO;
import com.feeltens.git.vo.resp.ListGitRepositoryRespVO;
import com.feeltens.git.vo.resp.ListOrganizationsRespVO;
import com.feeltens.git.vo.resp.PageGitBranchRespVO;
import com.feeltens.git.vo.resp.PageGitOrganizationRespVO;
import com.feeltens.git.vo.resp.PageGitProjectRespVO;
import com.feeltens.git.vo.resp.PageMixBranchRespVO;
import com.feeltens.git.vo.resp.QueryGitBranchRespVO;
import com.feeltens.git.vo.resp.QueryMixBranchRespVO;
import com.feeltens.git.vo.resp.RemergeMixBranchRespVO;
import com.feeltens.git.vo.resp.RemoveFromMixBranchRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * GitMergeFlowController
 *
 * @author feeltens
 * @date 2025-08-21
 */
@RestController
@RequestMapping("/api/v1/git-merge-flow")
@Slf4j
public class GitMergeFlowController {

    @Resource
    private GitFlowService gitFlowService;

    /**
     * 查询组织列表 (from open api)
     */
    @GetMapping("/listOrganizations")
    public CloudResponse<List<ListOrganizationsRespVO>> listOrganizations() {
        try {
            return gitFlowService.listOrganizations();
        } catch (Exception e) {
            log.error("listOrganizations hasError, e:", e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 分页查询git组织
     */
    @PostMapping("/pageGitOrganization")
    public CloudResponse<PageResponse<PageGitOrganizationRespVO>> pageGitOrganization(@RequestBody PageRequest<PageGitOrganizationReqVO> req) {
        PageResponse<PageGitOrganizationRespVO> res = gitFlowService.pageGitOrganization(req);
        // log.info("pageGitOrganization hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 列表查询git组织 (from db)
     */
    @PostMapping("/listGitOrganization")
    public CloudResponse<List<PageGitOrganizationRespVO>> listGitOrganization() {
        List<PageGitOrganizationRespVO> res = gitFlowService.listGitOrganization();
        return CloudResponse.success(res);
    }

    /**
     * 添加git组织
     */
    @PostMapping("/addGitOrganization")
    public CloudResponse<String> addGitOrganization(@RequestBody AddGitOrganizationReqVO req) {
        try {
            CloudResponse<String> res = gitFlowService.addGitOrganization(req);
            // log.info("addGitOrganization hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
            return res;
        } catch (Exception e) {
            log.error("addGitOrganization hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 添加git工程
     */
    @PostMapping("/addGitProject")
    public CloudResponse<String> addGitProject(@RequestBody AddGitProjectReqVO req) {
        try {
            long start = System.currentTimeMillis();
            CloudResponse<String> res = gitFlowService.addGitProject(req);
            log.info("addGitProject hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return res;
        } catch (Exception e) {
            log.error("addGitProject hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 分页查询git工程
     */
    @PostMapping("/pageGitProject")
    public CloudResponse<PageResponse<PageGitProjectRespVO>> pageGitProject(@RequestBody PageRequest<PageGitProjectReqVO> req) {
        PageResponse<PageGitProjectRespVO> res = gitFlowService.pageGitProject(req);
        // log.info("pageGitProject hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 列表查询git远程仓库 (open api)
     */
    @PostMapping("/listGitRepositoryNameByOpenApi")
    public CloudResponse<List<ListGitRepositoryRespVO>> listGitRepositoryNameByOpenApi(@RequestBody ListGitRepositoryReqVO req) {
        List<ListGitRepositoryRespVO> res = gitFlowService.listGitRepositoryNameByOpenApi(req);
        // log.info("listGitRepositoryNameByOpenApi hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 拉取远程分支，并且upsert到db
     */
    @PostMapping("/pullRemoteBranch")
    public CloudResponse<String> pullRemoteBranch(@RequestBody PullRemoteBranchReqVO req) {
        long start = System.currentTimeMillis();
        gitFlowService.pullRemoteBranch(req);
        log.info("pullRemoteBranch hasResult, param:{}    costTime:{}ms", JSON.toJSONString(req), System.currentTimeMillis() - start);
        return CloudResponse.success("success");
    }

    /**
     * 列表查询git工程 (db)
     */
    @PostMapping("/listGitProject")
    public CloudResponse<List<PageGitProjectRespVO>> listGitProject() {
        List<PageGitProjectRespVO> res = gitFlowService.listGitProject();
        // log.info("listGitProject hasResult, result:{}", JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 分页查询git原始分支
     */
    @PostMapping("/pageGitBranch")
    public CloudResponse<PageResponse<PageGitBranchRespVO>> pageGitBranch(@RequestBody PageRequest<PageGitBranchReqVO> req) {
        PageResponse<PageGitBranchRespVO> res = gitFlowService.pageGitBranch(req);
        // log.info("pageGitBranch hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 列表查询git原始分支
     */
    @PostMapping("/listGitBranch")
    public CloudResponse<List<ListGitBranchRespVO>> listGitBranch(@RequestBody ListGitBranchReqVO req) {
        List<ListGitBranchRespVO> res = gitFlowService.listGitBranch(req);
        // log.info("listGitBranch hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 单个查询git原始分支
     */
    @PostMapping("/queryGitBranch")
    public CloudResponse<QueryGitBranchRespVO> queryGitBranch(@RequestBody QueryGitBranchReqVO req) {
        QueryGitBranchRespVO res = gitFlowService.queryGitBranch(req);
        // log.info("queryGitBranch hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 修改git原始分支
     */
    @PostMapping("/updateGitBranch")
    public CloudResponse<Boolean> updateGitBranch(@RequestBody UpdateGitBranchReqVO req) {
        try {
            long start = System.currentTimeMillis();
            Boolean res = gitFlowService.updateGitBranch(req);
            log.info("updateGitBranch hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return CloudResponse.success(res);
        } catch (Exception e) {
            log.error("updateGitBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 创建git分支
     */
    @PostMapping("/createGitBranch")
    public CloudResponse<CreateGitBranchRespVO> createGitBranch(@RequestBody CreateGitBranchReqVO req) {
        try {
            long start = System.currentTimeMillis();
            CloudResponse<CreateGitBranchRespVO> res = gitFlowService.createGitBranch(req);
            log.info("createGitBranch hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return res;
        } catch (Exception e) {
            log.error("createGitBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 分页查询git中间分支
     */
    @PostMapping("/pageMixBranch")
    public CloudResponse<PageResponse<PageMixBranchRespVO>> pageMixBranch(@RequestBody PageRequest<PageMixBranchReqVO> req) {
        PageResponse<PageMixBranchRespVO> res = gitFlowService.pageMixBranch(req);
        // log.info("pageMixBranch hasResult, param:{}    result:{}", JSON.toJSONString(req), JSON.toJSONString(res));
        return CloudResponse.success(res);
    }

    /**
     * 查询git中间分支详情
     * <pre>
     *     1、入参：
     *     mixBranchId
     *
     *     2、出参：
     *     三大块内容：
     *     集成信息（集成分支名、集成状态、集成时间、集成人）
     *     已合并的分支列表（不包含主分支）
     *     未合并的分支列表（不包含主分支）
     * </pre>
     */
    @PostMapping("/queryMixBranch")
    public CloudResponse<QueryMixBranchRespVO> queryMixBranch(@RequestBody QueryMixBranchReqVO req) {
        try {
            // long start = System.currentTimeMillis();
            QueryMixBranchRespVO res = gitFlowService.queryMixBranch(req);
            // log.info("queryMixBranch hasResult, param:{}    result:{}    costTime:{}ms",
            //         JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return CloudResponse.success(res);
        } catch (Exception e) {
            log.error("queryMixBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 添加分支进git中间分支
     * <pre>
     *     gitProjectId、git中间分支名，List<源分支名称>、操作人
     * </pre>
     */
    @PostMapping("/addIntoMixBranch")
    public CloudResponse<AddIntoMixBranchRespVO> addIntoMixBranch(@RequestBody AddIntoMixBranchReqVO req) {
        try {
            long start = System.currentTimeMillis();
            AddIntoMixBranchRespVO res = gitFlowService.addIntoMixBranch(req);
            log.info("addIntoMixBranch hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return CloudResponse.success(res);
        } catch (Exception e) {
            log.error("addIntoMixBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 重新合并git中间分支
     */
    @PostMapping("/remergeMixBranch")
    public CloudResponse<RemergeMixBranchRespVO> remergeMixBranch(@RequestBody RemergeMixBranchReqVO req) {
        try {
            long start = System.currentTimeMillis();
            RemergeMixBranchRespVO res = gitFlowService.remergeMixBranch(req);
            log.info("remergeMixBranch hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return CloudResponse.success(res);
        } catch (Exception e) {
            log.error("remergeMixBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 从git中间分支移除源分支
     */
    @PostMapping("/removeFromMixBranch")
    public CloudResponse<RemoveFromMixBranchRespVO> removeFromMixBranch(@RequestBody RemoveFromMixBranchReqVO req) {
        try {
            long start = System.currentTimeMillis();
            RemoveFromMixBranchRespVO res = gitFlowService.removeFromMixBranch(req);
            log.info("removeFromMixBranch hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return CloudResponse.success(res);
        } catch (Exception e) {
            log.error("removeFromMixBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 删除git分支
     */
    @PostMapping("/deleteGitBranch")
    public CloudResponse<DeleteGitBranchRespVO> deleteGitBranch(@RequestBody DeleteGitBranchReqVO req) {
        try {
            long start = System.currentTimeMillis();
            DeleteGitBranchRespVO res = gitFlowService.deleteGitBranch(req);
            log.info("deleteGitBranch hasResult, param:{}    result:{}    costTime:{}ms",
                    JSON.toJSONString(req), JSON.toJSONString(res), System.currentTimeMillis() - start);
            return CloudResponse.success(res);
        } catch (Exception e) {
            log.error("deleteGitBranch hasError, param:{}    e:", JSON.toJSONString(req), e);
            return CloudResponse.fail(e.getMessage());
        }
    }

}