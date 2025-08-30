package com.feeltens.git.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.feeltens.git.common.constant.GitConstant;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.config.CodeupConfig;
import com.feeltens.git.config.GitLabConfig;
import com.feeltens.git.config.GitMergeFlowConfig;
import com.feeltens.git.converter.GitBranchConverter;
import com.feeltens.git.converter.GitMixBranchConverter;
import com.feeltens.git.converter.GitOrganizationConverter;
import com.feeltens.git.converter.GitProjectConverter;
import com.feeltens.git.dto.DoMergeBranchResultDTO;
import com.feeltens.git.dto.GitMixBranchDTO;
import com.feeltens.git.entity.GitBranchDO;
import com.feeltens.git.entity.GitMixBranchDO;
import com.feeltens.git.entity.GitMixBranchItemDO;
import com.feeltens.git.entity.GitOrganizationDO;
import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.enums.EnvEnum;
import com.feeltens.git.enums.GitServiceEnum;
import com.feeltens.git.enums.MergeTotalStatusEnum;
import com.feeltens.git.mapper.GitBranchMapper;
import com.feeltens.git.mapper.GitMixBranchItemMapper;
import com.feeltens.git.mapper.GitMixBranchMapper;
import com.feeltens.git.mapper.GitOrganizationMapper;
import com.feeltens.git.mapper.GitProjectMapper;
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
import com.feeltens.git.oapi.dto.req.OapiBaseReq;
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
import com.feeltens.git.oapi.factory.GitOpenApiFactory;
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
import com.feeltens.git.vo.req.ListGitRepositoryNameReqVO;
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
import com.feeltens.git.vo.resp.ListGitBranchRespVO;
import com.feeltens.git.vo.resp.ListOrganizationsRespVO;
import com.feeltens.git.vo.resp.PageGitBranchRespVO;
import com.feeltens.git.vo.resp.PageGitOrganizationRespVO;
import com.feeltens.git.vo.resp.PageGitProjectRespVO;
import com.feeltens.git.vo.resp.PageMixBranchRespVO;
import com.feeltens.git.vo.resp.QueryMixBranchRespVO;
import com.feeltens.git.vo.resp.RemergeMixBranchRespVO;
import com.feeltens.git.vo.resp.RemoveFromMixBranchRespVO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * git flow 服务组件
 *
 * @author feeltens
 * @date 2025-08-19
 */
@Service
@Slf4j
public class GitFlowServiceImpl implements GitFlowService {

    @Resource
    private GitMergeFlowConfig gitMergeFlowConfig;
    @Resource
    private CodeupConfig codeupConfig;
    @Resource
    private GitLabConfig gitLabConfig;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private GitOpenApiFactory gitOpenApiFactory;

    @Resource
    private GitOrganizationMapper gitOrganizationMapper;
    @Resource
    private GitProjectMapper gitProjectMapper;
    @Resource
    private GitBranchMapper gitBranchMapper;
    @Resource
    private GitMixBranchMapper gitMixBranchMapper;
    @Resource
    private GitMixBranchItemMapper gitMixBranchItemMapper;

    @Override
    public CloudResponse<List<ListOrganizationsRespVO>> listOrganizations() {
        ListOrganizationsReq request = new ListOrganizationsReq();
        request.setBaseUrl(codeupConfig.getBaseUrl());
        request.setAccessToken(codeupConfig.getAccessToken());

        ListOrganizationsResp oapiResponse = gitOpenApiFactory.listOrganizations(request);
        if (null == oapiResponse || CollUtil.isEmpty(oapiResponse.getOrganizationItemList())) {
            return CloudResponse.fail("未找到组织信息");
        }

        List<ListOrganizationsRespVO> list = Lists.newArrayList();
        for (ListOrganizationsResp.ListOrganizationsRespItem item : oapiResponse.getOrganizationItemList()) {
            ListOrganizationsRespVO vo = new ListOrganizationsRespVO();
            vo.setOrganizationId(item.getId());
            vo.setOrganizationName(item.getName());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            list.add(vo);
        }
        return CloudResponse.success(list);
    }

    @Override
    public PageResponse<PageGitOrganizationRespVO> pageGitOrganization(PageRequest<PageGitOrganizationReqVO> req) {
        if (req.getCurrentPage() < 0) {
            req.setCurrentPage(1);
        }
        if (req.getPageSize() > 200) {
            req.setPageSize(200);
        }

        if (null == req.getParam()) {
            req.setParam(new PageGitOrganizationReqVO());
        }

        // 处理参数：去除字符串左右空格
        req.getParam().setGitOrganizationName(StrUtil.trim(req.getParam().getGitOrganizationName()));

        Long totalCount = gitOrganizationMapper.countOrganization(req.getParam());
        if (totalCount <= 0) {
            return PageResponse.build(Collections.emptyList(), req.getCurrentPage(), req.getPageSize(), 0L);
        }

        Integer currentPage = req.getCurrentPage();
        Integer pageSize = req.getPageSize();
        Integer limitSize = null;
        // currentPage、pageSize 为空时，则查询全部
        if (null != currentPage && null != pageSize) {
            limitSize = (currentPage - 1) * pageSize;
        }
        List<GitOrganizationDO> list = gitOrganizationMapper.pageOrganization(req.getParam(), limitSize, pageSize);
        return PageResponse.build(GitOrganizationConverter.toPageVos(list), req.getCurrentPage(), req.getPageSize(), totalCount);
    }

    @Override
    public List<PageGitOrganizationRespVO> listGitOrganization() {
        List<GitOrganizationDO> list = gitOrganizationMapper.listOrganization();

        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.CODEUP.getCode())) {
            return GitOrganizationConverter.toPageVos(list);
        }

        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.GITLAB.getCode())) {
            // gitlab 的 open-api 不需要组织ID，所以这里返回一个默认的
            PageGitOrganizationRespVO vo = new PageGitOrganizationRespVO();
            // vo.setOrgInnerId();
            vo.setOrganizationId("default");
            vo.setOrganizationName("default");
            return Lists.newArrayList(vo);
        }

        return null;
    }

    @Override
    public CloudResponse<String> addGitOrganization(AddGitOrganizationReqVO req) {
        // 校验参数
        if (null == req) {
            throw new RuntimeException("入参为空");
        }
        req.setOrganizationId(StrUtil.trim(req.getOrganizationId()));
        req.setOperator(StrUtil.trim(req.getOperator()));
        if (StrUtil.isEmptyIfStr(req.getOrganizationId())) {
            throw new RuntimeException("组织id不能为空");
        }
        if (StrUtil.isEmptyIfStr(req.getOperator())) {
            throw new RuntimeException("操作人不能为空");
        }

        // 根据组织id，查询db，确保工程名唯一
        GitOrganizationDO gitOrganizationDb = gitOrganizationMapper.queryByOrganizationId(req.getOrganizationId());
        if (null != gitOrganizationDb) {
            return CloudResponse.fail(gitOrganizationDb.getOrganizationName() + "已存在");
        }

        // 调用open api，列表查询组织
        CloudResponse<List<ListOrganizationsRespVO>> listCloudResponse = listOrganizations();
        if (null == listCloudResponse
                || CollUtil.isEmpty(listCloudResponse.getData())) {
            throw new BizException("调用open api，但组织不存在");
        }

        // 过滤 组织id
        Optional<ListOrganizationsRespVO> firstOpt = listCloudResponse.getData().stream()
                .filter(each -> null != each && each.getOrganizationId().equals(req.getOrganizationId()))
                .findFirst();
        if (!firstOpt.isPresent()) {
            throw new BizException("组织不存在:" + req.getOrganizationId());
        }

        ListOrganizationsRespVO organizationsVO = firstOpt.get();

        // 落库s
        GitOrganizationDO gitOrganizationDO = new GitOrganizationDO();
        // gitOrganizationDO.setOrgInnerId(); // auto-incr
        gitOrganizationDO.setOrganizationId(organizationsVO.getOrganizationId());
        gitOrganizationDO.setOrganizationName(organizationsVO.getOrganizationName());
        gitOrganizationDO.setDescription(organizationsVO.getDescription());
        gitOrganizationDO.setWebUrl(null);
        gitOrganizationDO.setCreateBy(req.getOperator());
        gitOrganizationDO.setCreateTime(organizationsVO.getCreateTime());
        gitOrganizationDO.setUpdateBy(req.getOperator());
        gitOrganizationDO.setUpdateTime(organizationsVO.getUpdateTime());
        gitOrganizationDO.setDeleted(0L);

        gitOrganizationMapper.insert(gitOrganizationDO);

        return CloudResponse.success("添加成功");
    }

    private void setGitMergeFlowConfig(OapiBaseReq req, String organizationId) {
        // codeup
        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.CODEUP.getCode())) {
            req.setBaseUrl(codeupConfig.getBaseUrl());
            req.setAccessToken(codeupConfig.getAccessToken());
            req.setOrganizationId(organizationId);
            return;
        }
        // gitlab
        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.GITLAB.getCode())) {
            req.setBaseUrl(gitLabConfig.getBaseUrl());
            req.setAccessToken(gitLabConfig.getAccessToken());
            return;
        }

        throw new BizException("未识别的git服务平台");
    }

    @Override
    public CloudResponse<String> addGitProject(AddGitProjectReqVO req) {
        // 落库的工程id
        Long projectId = null;
        List<Long> mixBranchIdList = Lists.newArrayList();
        try {
            // 校验参数
            if (null == req) {
                throw new RuntimeException("入参为空");
            }
            req.setOrganizationId(StrUtil.trim(req.getOrganizationId()));
            req.setRepositoryName(StrUtil.trim(req.getRepositoryName()));
            req.setProjectName(StrUtil.trim(req.getProjectName()));
            req.setOperator(StrUtil.trim(req.getOperator()));
            if (StrUtil.isEmptyIfStr(req.getOrganizationId())) {
                throw new RuntimeException("组织id不能为空");
            }
            if (StrUtil.isEmptyIfStr(req.getRepositoryName())) {
                throw new RuntimeException("请选择仓库");
            }
            if (StrUtil.isEmptyIfStr(req.getProjectName())) {
                throw new RuntimeException("工程名称不能为空");
            }
            if (StrUtil.isEmptyIfStr(req.getOperator())) {
                throw new RuntimeException("操作人不能为空");
            }

            // 根据工程名，查询db，确保工程名唯一
            GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectName(req.getProjectName());
            if (null != gitProjectDb) {
                return CloudResponse.fail(req.getProjectName() + "工程已存在");
            }

            // 调用open api，查询git仓库信息
            ListRepositoriesReq request = new ListRepositoriesReq(req.getRepositoryName()); // 仓库名称
            setGitMergeFlowConfig(request, req.getOrganizationId());

            ListRepositoriesResp oapiResponse = gitOpenApiFactory.listRepositories(request);
            if (null == oapiResponse || CollUtil.isEmpty(oapiResponse.getRepoItemList())) {
                return CloudResponse.fail("未找到对应的仓库信息:" + req.getProjectName());
            }
            if (oapiResponse.getRepoItemList().size() > 1) {
                return CloudResponse.fail("匹配到多个仓库，请检查:" + req.getProjectName());
            }

            // 仓库id
            Long repositoryId = oapiResponse.getRepoItemList().get(0).getId();

            // 根据git仓库id，查询git仓库信息
            GetRepositoryReq getRepositoryReq = new GetRepositoryReq();
            setGitMergeFlowConfig(getRepositoryReq, req.getOrganizationId());
            getRepositoryReq.setRepositoryId(repositoryId);
            GetRepositoryResp repository = gitOpenApiFactory.getRepository(getRepositoryReq);
            if (null == repository) {
                return CloudResponse.fail("未找到对应的仓库信息:" + req.getProjectName());
            }

            GitProjectDO gitProjectDO = buildGitProject(repository, req.getOrganizationId(), req.getOperator());
            // 提前落库，后续会用到 gitProjectId
            int insertRow = gitProjectMapper.insert(gitProjectDO);
            if (insertRow <= 0) {
                throw new RuntimeException("落库失败:gitProject");
            }
            projectId = gitProjectDO.getProjectId();

            // 拉取所有远程分支，并且upsert到db
            fetchOriginalBranchList(gitProjectDO, repositoryId);

            for (EnvEnum envEnum : EnvEnum.values()) {
                String mixBranchName = getMixBranchName(envEnum);

                // 重建git中间分支
                Long newMixBranchId = recreateMixBranch(gitProjectDO.getProjectId(), mixBranchName,
                        envEnum, Lists.newArrayList(gitProjectDO.getDefaultBranch()), req.getOperator());
                mixBranchIdList.add(newMixBranchId);

                // 重新集成
                RemergeMixBranchReqVO remergeMixBranchReqVO = new RemergeMixBranchReqVO();
                remergeMixBranchReqVO.setMixBranchId(newMixBranchId);
                remergeMixBranchReqVO.setOperator(req.getOperator());
                RemergeMixBranchRespVO remergeMixBranchRespVO = remergeMixBranch(remergeMixBranchReqVO);
            }

            return CloudResponse.success();
        } catch (Exception e) {
            // 有异常，则删除本次产生的数据
            if (null != projectId) {
                gitProjectMapper.deleteByProjectId(projectId);
                gitBranchMapper.deleteByProjectId(projectId);
                gitMixBranchMapper.deleteByProjectId(projectId);
            }
            if (CollUtil.isNotEmpty(mixBranchIdList)) {
                gitMixBranchItemMapper.deleteByMixBranchIds(mixBranchIdList);
            }
            throw e;
        }
    }

    private GitMixBranchDTO buildGitMixBranchDTO(String mixBranchName,
                                                 EnvEnum envEnum,
                                                 String defaultBranchName,
                                                 List<String> itemBranchNameList,
                                                 String operator) {
        Date now = new Date();

        List<GitMixBranchItemDO> mixBranchItemList = Lists.newArrayList();
        for (String branchName : itemBranchNameList) {
            GitMixBranchItemDO gitMixBranchItemDO = new GitMixBranchItemDO();
            // gitMixBranchItemDO.setMixBranchItemId(); // auto-incr
            // gitMixBranchItemDO.setMixBranchId(); // set later
            gitMixBranchItemDO.setBranchName(branchName);
            // 中间分支是基于主分支创建的，所以mergeFlag=1
            gitMixBranchItemDO.setMergeFlag(StrUtil.equals(branchName, defaultBranchName) ? 1 : 0);
            gitMixBranchItemDO.setCreateBy(operator);
            gitMixBranchItemDO.setCreateTime(now);
            gitMixBranchItemDO.setUpdateBy(operator);
            gitMixBranchItemDO.setUpdateTime(now);
            gitMixBranchItemDO.setDeleted(0L);
            mixBranchItemList.add(gitMixBranchItemDO);
        }

        GitMixBranchDTO gitMixBranchDTO = new GitMixBranchDTO();
        // gitMixBranchDTO.setMixBranchId(); // auto-incr
        gitMixBranchDTO.setMixBranchName(mixBranchName);
        gitMixBranchDTO.setEnv(envEnum.getCode());
        // gitMixBranchDTO.setProjectId(); // set later
        gitMixBranchDTO.setAllMergeFlag(1);
        gitMixBranchDTO.setCreateBy(operator);
        gitMixBranchDTO.setCreateTime(now);
        gitMixBranchDTO.setUpdateBy(operator);
        gitMixBranchDTO.setUpdateTime(now);
        gitMixBranchDTO.setDeleted(0L);
        gitMixBranchDTO.setMixBranchExt(null);
        gitMixBranchDTO.setMixBranchItemList(mixBranchItemList);
        return gitMixBranchDTO;
    }

    /**
     * 获取中间分支名
     * <pre>
     *     dev_mix
     *     test_mix
     *     pre_mix
     * </pre>
     *
     * @param envEnum 环境枚举
     * @return
     */
    private String getMixBranchName(EnvEnum envEnum) {
        assert null != envEnum;
        return envEnum.getCode() + GitConstant.MIX_BRANCH_NAME_SUFFIX;
    }

    @Override
    public CloudResponse<CreateGitBranchRespVO> createGitBranch(CreateGitBranchReqVO req) {
        // 校验参数
        if (null == req) {
            throw new RuntimeException("入参为空");
        }
        req.setGitBranchName(StrUtil.trim(req.getGitBranchName()));
        req.setBranchDesc(StrUtil.trim(req.getBranchDesc()));
        req.setSourceBranch(StrUtil.trim(req.getSourceBranch()));
        req.setOperator(StrUtil.trim(req.getOperator()));
        if (StrUtil.isEmptyIfStr(req.getGitProjectId())) {
            throw new RuntimeException("git工程id不能为空");
        }
        if (StrUtil.isEmptyIfStr(req.getGitBranchName())) {
            throw new RuntimeException("分支名称不能为空");
        }
        if (StrUtil.length(req.getGitBranchName()) > 100) {
            throw new RuntimeException("分支名称不能超过100字符");
        }
        if (StrUtil.isEmptyIfStr(req.getBranchDesc())) {
            throw new RuntimeException("分支描述不能为空");
        }
        if (StrUtil.length(req.getGitBranchName()) > 200) {
            throw new RuntimeException("分支描述不能超过200字符");
        }
        // if (StrUtil.isEmptyIfStr(req.getSourceBranch())) {
        //     throw new RuntimeException("创建来源分支名称不能为空");
        // }
        if (StrUtil.isEmptyIfStr(req.getOperator())) {
            throw new RuntimeException("操作人不能为空");
        }

        // 根据工程id，查询git工程信息
        GitProjectDO gitProjectDO = gitProjectMapper.selectByProjectId(req.getGitProjectId());
        if (null == gitProjectDO) {
            throw new RuntimeException("git工程不存在");
        }
        // 如果没传源分支，则默认使用默认分支
        if (StrUtil.isEmptyIfStr(req.getSourceBranch())) {
            req.setSourceBranch(gitProjectDO.getDefaultBranch());
        }

        GitBranchDO gitBranchDb = gitBranchMapper.queryByBranchName(req.getGitBranchName());
        if (null != gitBranchDb) {
            throw new RuntimeException(req.getGitBranchName() + "分支已存在，无需重复添加");
        }

        // 调用 api 创建分支
        CreateBranchResp createBranchResp = createNewBranch(gitProjectDO.getRepositoryId(), gitProjectDO.getOrganizationId(), req.getSourceBranch(), req.getGitBranchName());

        // 落库: git_branch
        GitBranchDO gitBranchDO = buildGitBranchDO(req, createBranchResp);
        int insertRow = gitBranchMapper.insertList(Lists.newArrayList(gitBranchDO));
        if (insertRow <= 0) {
            throw new RuntimeException("创建分支落库失败:" + req.getGitBranchName());
        }

        return CloudResponse.success(CreateGitBranchRespVO.builder().gitBranchName(req.getGitBranchName()).build());
    }

    @Override
    public PageResponse<PageGitProjectRespVO> pageGitProject(PageRequest<PageGitProjectReqVO> req) {
        if (req.getCurrentPage() < 0) {
            req.setCurrentPage(1);
        }
        if (req.getPageSize() > 200) {
            req.setPageSize(200);
        }

        if (null == req.getParam()) {
            req.setParam(new PageGitProjectReqVO());
        }

        // 处理参数：去除字符串左右空格
        req.getParam().setGitProjectName(StrUtil.trim(req.getParam().getGitProjectName()));

        Long totalCount = gitProjectMapper.countProject(req.getParam());
        if (totalCount <= 0) {
            return PageResponse.build(Collections.emptyList(), req.getCurrentPage(), req.getPageSize(), 0L);
        }

        Integer currentPage = req.getCurrentPage();
        Integer pageSize = req.getPageSize();
        Integer limitSize = null;
        // currentPage、pageSize 为空时，则查询全部
        if (null != currentPage && null != pageSize) {
            limitSize = (currentPage - 1) * pageSize;
        }
        List<GitProjectDO> list = gitProjectMapper.pageProject(req.getParam(), limitSize, pageSize);
        List<PageGitProjectRespVO> pageVoList = GitProjectConverter.toPageVos(list);
        if (CollUtil.isNotEmpty(pageVoList)) {
            List<GitMixBranchDO> gitMixBranchList = gitMixBranchMapper.pageMixBranch(new PageMixBranchReqVO(), null, null);
            for (PageGitProjectRespVO pageVO : pageVoList) {
                for (EnvEnum envEnum : EnvEnum.values()) {
                    Optional<GitMixBranchDO> first = gitMixBranchList.stream()
                            .filter(item -> item.getProjectId().equals(pageVO.getProjectId()) && item.getEnv().equals(envEnum.getCode())).findFirst();
                    if (!first.isPresent()) {
                        continue;
                    }

                    GitMixBranchDO gitMixBranchDO = first.get();
                    switch (envEnum) {
                        case DEV:
                            pageVO.setDevMixBranchId(gitMixBranchDO.getMixBranchId());
                            break;
                        case TEST:
                            pageVO.setTestMixBranchId(gitMixBranchDO.getMixBranchId());
                            break;
                        case PRE:
                            pageVO.setPreMixBranchId(gitMixBranchDO.getMixBranchId());
                            break;
                    }
                }
            }
        }
        return PageResponse.build(pageVoList, req.getCurrentPage(), req.getPageSize(), totalCount);
    }

    @Override
    public List<String> listGitRepositoryNameByOpenApi(ListGitRepositoryNameReqVO req) {
        // 调用open api，查询git仓库信息
        ListRepositoriesReq request = new ListRepositoriesReq(); // 仓库名称
        setGitMergeFlowConfig(request, req.getOrganizationId());

        ListRepositoriesResp oapiResponse = gitOpenApiFactory.listRepositories(request);
        if (null == oapiResponse || CollUtil.isEmpty(oapiResponse.getRepoItemList())) {
            throw new BizException("未找到仓库信息 organizationId:" + req.getOrganizationId());
        }

        return oapiResponse.getRepoItemList().stream()
                .map(ListRepositoriesResp.ListRepositoriesRespItem::getName).collect(Collectors.toList());
    }

    @Override
    public List<PageGitProjectRespVO> listGitProject() {
        List<GitProjectDO> list = gitProjectMapper.pageProject(new PageGitProjectReqVO(), null, null);
        return GitProjectConverter.toPageVos(list);
    }

    @Override
    public PageResponse<PageMixBranchRespVO> pageMixBranch(PageRequest<PageMixBranchReqVO> req) {
        if (req.getCurrentPage() < 0) {
            req.setCurrentPage(1);
        }
        if (req.getPageSize() > 200) {
            req.setPageSize(200);
        }

        if (null == req.getParam()) {
            req.setParam(new PageMixBranchReqVO());
        }

        // 处理参数：去除字符串左右空格
        req.getParam().setMixBranchName(StrUtil.trim(req.getParam().getMixBranchName()));

        // 参数校验
        if (null == req.getParam().getGitProjectId()) {
            throw new BizException("参数错误：git工程id不能为空");
        }

        Long totalCount = gitMixBranchMapper.countMixBranch(req.getParam());
        if (totalCount <= 0) {
            return PageResponse.build(Collections.emptyList(), req.getCurrentPage(), req.getPageSize(), 0L);
        }

        Integer currentPage = req.getCurrentPage();
        Integer pageSize = req.getPageSize();
        Integer limitSize = null;
        // currentPage、pageSize 为空时，则查询全部
        if (null != currentPage && null != pageSize) {
            limitSize = (currentPage - 1) * pageSize;
        }
        List<GitMixBranchDO> list = gitMixBranchMapper.pageMixBranch(req.getParam(), limitSize, pageSize);
        return PageResponse.build(GitMixBranchConverter.toPageVos(list), req.getCurrentPage(), req.getPageSize(), totalCount);
    }

    @Override
    public PageResponse<PageGitBranchRespVO> pageGitBranch(PageRequest<PageGitBranchReqVO> req) {
        if (req.getCurrentPage() < 0) {
            req.setCurrentPage(1);
        }
        if (req.getPageSize() > 200) {
            req.setPageSize(200);
        }

        if (null == req.getParam()) {
            req.setParam(new PageGitBranchReqVO());
        }

        // 处理参数：去除字符串左右空格
        req.getParam().setBranchName(StrUtil.trim(req.getParam().getBranchName()));

        // 参数校验
        if (null == req.getParam().getGitProjectId()) {
            throw new BizException("参数错误：git工程id不能为空");
        }

        Long totalCount = gitBranchMapper.countBranch(req.getParam());
        if (totalCount <= 0) {
            return PageResponse.build(Collections.emptyList(), req.getCurrentPage(), req.getPageSize(), 0L);
        }

        Integer currentPage = req.getCurrentPage();
        Integer pageSize = req.getPageSize();
        Integer limitSize = null;
        // currentPage、pageSize 为空时，则查询全部
        if (null != currentPage && null != pageSize) {
            limitSize = (currentPage - 1) * pageSize;
        }
        List<GitBranchDO> list = gitBranchMapper.pageBranch(req.getParam(), limitSize, pageSize);
        return PageResponse.build(GitBranchConverter.toPageVos(list), req.getCurrentPage(), req.getPageSize(), totalCount);
    }

    @Override
    public List<ListGitBranchRespVO> listGitBranch(ListGitBranchReqVO req) {
        // 校验入参
        if (null == req || null == req.getGitProjectId()) {
            throw new BizException("工程id必填");
        }
        req.setBranchName(StrUtil.trim(req.getBranchName()));

        GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectId(req.getGitProjectId());

        List<GitBranchDO> branchList = gitBranchMapper.queryByProjectIdAndBranchName(req);
        if (CollUtil.isEmpty(branchList)) {
            return Collections.emptyList();
        }

        List<GitBranchDO> list = Lists.newArrayList();
        for (GitBranchDO gitBranchDO : branchList) {
            // 中间分支，则跳过
            if (GitConstant.EXCLUDE_BRANCH_NAME_LIST.contains(gitBranchDO.getBranchName())) {
                continue;
            }
            // 备份分支，则跳过
            if (gitBranchDO.getBranchName().contains("gitbackup_xim")) {
                continue;
            }

            list.add(gitBranchDO);
        }

        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 不需要匹配中间分支id
        if (null == req.getMixBranchId()) {
            return GitBranchConverter.toListVos(list);
        }

        // 需要匹配中间分支id
        List<GitMixBranchItemDO> mixBranchItemDOList = gitMixBranchItemMapper.queryByMixBranchIdAndBranchName(req);
        if (CollUtil.isEmpty(mixBranchItemDOList)) {
            return Collections.emptyList();
        }

        // Map<分支名称, 是否合并的标识>
        Map<String, Integer> branckName2MergeFlagMap = mixBranchItemDOList.stream()
                .collect(Collectors.toMap(GitMixBranchItemDO::getBranchName, GitMixBranchItemDO::getMergeFlag));
        list = list.stream().filter(each -> branckName2MergeFlagMap.containsKey(each.getBranchName())).collect(Collectors.toList());
        List<ListGitBranchRespVO> resultList = GitBranchConverter.toListVos(list);
        if (CollUtil.isEmpty(resultList)) {
            return resultList;
        }
        // 已集成，则以 合并标识 为准
        for (ListGitBranchRespVO vo : resultList) {
            vo.setMergeStatus(branckName2MergeFlagMap.get(vo.getBranchName()));
        }
        return resultList;
    }

    @Override
    public QueryMixBranchRespVO queryMixBranch(QueryMixBranchReqVO req) {
        // 校验入参
        if (null == req || null == req.getMixBranchId()) {
            throw new BizException("未找到中间分支，请联系管理员");
        }

        GitMixBranchDO mixBranchDb = gitMixBranchMapper.queryByMixBranchId(req.getMixBranchId());
        if (null == mixBranchDb) {
            throw new BizException("未找到中间分支，数据库不存在 " + req.getMixBranchId());
        }

        GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectId(mixBranchDb.getProjectId());
        if (null == gitProjectDb) {
            throw new BizException("未找到git工程，数据库不存在 " + req.getMixBranchId());
        }

        // 所有git原始分支
        List<GitBranchDO> gitBranchList = gitBranchMapper.queryByProjectId(mixBranchDb.getProjectId());
        if (CollUtil.isEmpty(gitBranchList)) {
            throw new BizException("未找到git分支，数据库不存在 " + req.getMixBranchId());
        }

        // 当前集成分支
        List<GitMixBranchItemDO> mixBranchItemList = gitMixBranchItemMapper.queryByMixBranchId(req.getMixBranchId());
        if (CollUtil.isEmpty(mixBranchItemList)) {
            throw new BizException("未找到当前集成分支，数据库不存在 " + req.getMixBranchId());
        }

        List<GitMixBranchItemDO> mergeFailedItemList = mixBranchItemList.stream().filter(each -> null != each
                && Objects.equals(0, each.getMergeFlag())).collect(Collectors.toList());

        // 总集成状态：以 gitMixBranchItem 为准
        Integer allMergeFlag = CollUtil.isEmpty(mergeFailedItemList) ? 1 : 0;
        String userMergeShell = "";
        if (CollUtil.isNotEmpty(mergeFailedItemList)) {
            userMergeShell = getUserMergeShell(mergeFailedItemList.get(0).getBranchName(), mixBranchDb.getMixBranchName());
        }

        // 当前集成分支
        List<GitBranchDO> mixedBranchList = Lists.newArrayList();
        // 待集成分支
        List<GitBranchDO> toMixBranchList = Lists.newArrayList();

        // 当前集成的分支名 --> 合并状态
        Map<String, Integer> mixBranchName2MergeFlagMap = mixBranchItemList.stream()
                .collect(Collectors.toMap(GitMixBranchItemDO::getBranchName, GitMixBranchItemDO::getMergeFlag));
        for (GitBranchDO gitBranchDO : gitBranchList) {
            // 默认分支，则跳过
            if (gitProjectDb.getDefaultBranch().equals(gitBranchDO.getBranchName())) {
                continue;
            }
            // 中间分支，则跳过
            if (GitConstant.EXCLUDE_BRANCH_NAME_LIST.contains(gitBranchDO.getBranchName())) {
                continue;
            }
            // 备份分支，则跳过
            if (gitBranchDO.getBranchName().contains("gitbackup_xim")) {
                continue;
            }
            if (mixBranchName2MergeFlagMap.containsKey(gitBranchDO.getBranchName())) {
                mixedBranchList.add(gitBranchDO);
            } else {
                toMixBranchList.add(gitBranchDO);
            }
        }

        QueryMixBranchRespVO queryMixBranchRespVO = new QueryMixBranchRespVO();
        queryMixBranchRespVO.setMixBranchId(mixBranchDb.getMixBranchId());
        queryMixBranchRespVO.setMixBranchName(mixBranchDb.getMixBranchName());
        queryMixBranchRespVO.setEnv(mixBranchDb.getEnv());
        queryMixBranchRespVO.setProjectId(mixBranchDb.getProjectId());
        queryMixBranchRespVO.setAllMergeFlag(allMergeFlag);
        queryMixBranchRespVO.setUserMergeShell(allMergeFlag == 1 ? "" : userMergeShell);
        queryMixBranchRespVO.setCreateBy(mixBranchDb.getCreateBy());
        queryMixBranchRespVO.setCreateTime(mixBranchDb.getCreateTime());
        queryMixBranchRespVO.setUpdateBy(mixBranchDb.getUpdateBy());
        queryMixBranchRespVO.setUpdateTime(mixBranchDb.getUpdateTime());
        queryMixBranchRespVO.setMixedBranchVoList(GitBranchConverter.convertMixedBranchVoList(mixedBranchList, mixBranchName2MergeFlagMap));
        queryMixBranchRespVO.setToMixBranchVoList(GitBranchConverter.convertToMixBranchVoList(toMixBranchList));

        return queryMixBranchRespVO;
    }

    @Override
    public RemergeMixBranchRespVO remergeMixBranch(RemergeMixBranchReqVO req) {
        // 校验入参
        if (null == req || null == req.getMixBranchId()) {
            throw new BizException("未找到中间分支，请联系管理员");
        }
        req.setOperator(StrUtil.trim(req.getOperator()));
        if (StrUtil.isEmptyIfStr(req.getOperator())) {
            throw new BizException("操作人不能为空");
        }

        GitMixBranchDO mixBranchDb = gitMixBranchMapper.queryByMixBranchId(req.getMixBranchId());
        if (null == mixBranchDb) {
            throw new BizException("未找到中间分支，数据库不存在 " + req.getMixBranchId());
        }

        GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectId(mixBranchDb.getProjectId());
        if (null == gitProjectDb) {
            throw new BizException("未找到git工程，数据库不存在 " + req.getMixBranchId());
        }

        // 当前集成分支
        List<GitMixBranchItemDO> mixBranchItemList = gitMixBranchItemMapper.queryByMixBranchId(req.getMixBranchId());
        // if (CollUtil.isEmpty(mixBranchItemList)) {
        //     throw new BizException("未找到当前集成分支，数据库不存在 " + req.getMixBranchId());
        // }

        // 待移除的分支名list
        List<String> removeBranchItemNameList = Lists.newArrayList();
        // 待集成的分支名list
        List<String> mixedBranchNameList = Lists.newArrayList();

        // 1、当前集成分支是否已合并进主分支？已进主分支，则不再参与合并。
        if (CollUtil.isNotEmpty(mixBranchItemList)) {
            for (GitMixBranchItemDO mixBranchItem : mixBranchItemList) {
                // 主分支，则跳过
                if (StrUtil.equalsIgnoreCase(gitProjectDb.getDefaultBranch(), mixBranchItem.getBranchName())) {
                    continue;
                }

                GetCompareReq getCompareReq = new GetCompareReq();
                setGitMergeFlowConfig(getCompareReq, gitProjectDb.getOrganizationId());
                getCompareReq.setRepositoryId(gitProjectDb.getRepositoryId());
                getCompareReq.setFrom(gitProjectDb.getDefaultBranch());
                getCompareReq.setTo(mixBranchItem.getBranchName());
                // 调用 open api，查询代码比较内容，判断当前分支是否已合并进主分支
                GetCompareResp getCompareResp = gitOpenApiFactory.getCompare(getCompareReq);
                if (CollUtil.isEmpty(getCompareResp.getCommits())) {
                    // 当前分支已合并进主分支
                    removeBranchItemNameList.add(mixBranchItem.getBranchName());
                } else {
                    mixedBranchNameList.add(mixBranchItem.getBranchName());
                }
            }
        }

        // 2、合并主分支到中间分支
        DoMergeBranchResultDTO mergeResultDTO = doMergeIntoMixBranch(
                req.getMixBranchId(), gitProjectDb.getRepositoryId(), gitProjectDb.getOrganizationId(),
                gitProjectDb.getDefaultBranch(), mixBranchDb.getMixBranchName(), req.getOperator());
        if (!mergeResultDTO.getFlag()) {
            int updateRow = gitMixBranchMapper.updateAllMergeFlag(req.getMixBranchId(), 0, req.getOperator());
            // 自动合并失败，则返回手动集成shell
            return GitMixBranchConverter.toRespVo(mergeResultDTO);
        }

        // 3、合并已集成的分支到中间分支
        if (CollUtil.isNotEmpty(mixedBranchNameList)) {
            for (String branchName : mixedBranchNameList) {
                // 合并源分支到目标分支
                DoMergeBranchResultDTO mergeBranchResultDTO = doMergeIntoMixBranch(
                        req.getMixBranchId(), gitProjectDb.getRepositoryId(), gitProjectDb.getOrganizationId(), branchName,
                        mixBranchDb.getMixBranchName(), req.getOperator());
                if (!mergeBranchResultDTO.getFlag()) {
                    int updateRow = gitMixBranchMapper.updateAllMergeFlag(req.getMixBranchId(), 0, req.getOperator());
                    // 自动合并失败，则返回手动集成shell
                    return GitMixBranchConverter.toRespVo(mergeBranchResultDTO);
                }
            }
        }

        // 落库
        if (CollUtil.isNotEmpty(removeBranchItemNameList)) {
            int row = gitMixBranchItemMapper.deleteByMixBranchIdAndBranchNameList(req.getMixBranchId(), removeBranchItemNameList);
        }

        // 全部成功
        int updateRow = gitMixBranchMapper.updateAllMergeFlag(req.getMixBranchId(), 1, req.getOperator());
        return new RemergeMixBranchRespVO(req.getMixBranchId(), true, null);
    }

    @Override
    public AddIntoMixBranchRespVO addIntoMixBranch(AddIntoMixBranchReqVO req) {
        // 校验入参
        if (null == req || null == req.getMixBranchId()) {
            throw new BizException("未找到中间分支，请联系管理员");
        }
        req.setOperator(StrUtil.trim(req.getOperator()));
        if (StrUtil.isEmptyIfStr(req.getOperator())) {
            throw new BizException("操作人不能为空");
        }
        if (CollUtil.isEmpty(req.getGitBranchNameList())) {
            throw new BizException("请选择要添加的分支");
        }

        GitMixBranchDO mixBranchDb = gitMixBranchMapper.queryByMixBranchId(req.getMixBranchId());
        if (null == mixBranchDb) {
            throw new BizException("未找到中间分支，数据库不存在 " + req.getMixBranchId());
        }

        GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectId(mixBranchDb.getProjectId());
        if (null == gitProjectDb) {
            throw new BizException("未找到git工程，数据库不存在 " + req.getMixBranchId());
        }

        // 当前集成分支
        List<GitMixBranchItemDO> mixBranchItemList = gitMixBranchItemMapper.queryByMixBranchId(req.getMixBranchId());
        if (CollUtil.isEmpty(mixBranchItemList)) {
            throw new BizException("未找到当前集成分支，数据库不存在 " + req.getMixBranchId());
        }
        List<String> mergedBranchNameList = mixBranchItemList.stream()
                .filter(each -> null != each && null != each.getMergeFlag() && each.getMergeFlag() == 1)
                .map(GitMixBranchItemDO::getBranchName).collect(Collectors.toList());

        // 需要集成的分支名list
        List<String> needMixBranchNameList = req.getGitBranchNameList().stream()
                .filter(each -> !mergedBranchNameList.contains(each)).collect(Collectors.toList());
        // 默认每次都需要集成主分支
        if (!needMixBranchNameList.contains(gitProjectDb.getDefaultBranch())) {
            needMixBranchNameList.add(gitProjectDb.getDefaultBranch());
        }
        if (CollUtil.isEmpty(needMixBranchNameList)) {
            throw new RuntimeException("选择的分支已集成。");
        }

        Date now = new Date();
        for (String branchName : needMixBranchNameList) {
            GitMixBranchItemDO gitMixBranchItemDO = new GitMixBranchItemDO();
            // gitMixBranchItemDO.setMixBranchItemId(); // auto-incr
            gitMixBranchItemDO.setMixBranchId(req.getMixBranchId());
            gitMixBranchItemDO.setBranchName(branchName);
            gitMixBranchItemDO.setMergeFlag(0);
            gitMixBranchItemDO.setCreateBy(req.getOperator());
            gitMixBranchItemDO.setCreateTime(now);
            gitMixBranchItemDO.setUpdateBy(req.getOperator());
            gitMixBranchItemDO.setUpdateTime(now);
            gitMixBranchItemDO.setDeleted(0L);
            gitMixBranchItemMapper.insertOrUpdateByBranchName(gitMixBranchItemDO);
        }

        for (String branchName : needMixBranchNameList) {
            // 合并源分支到目标分支
            DoMergeBranchResultDTO mergeBranchResultDTO = doMergeIntoMixBranch(
                    req.getMixBranchId(), gitProjectDb.getRepositoryId(), gitProjectDb.getOrganizationId(), branchName, mixBranchDb.getMixBranchName(), req.getOperator());
            if (!mergeBranchResultDTO.getFlag()) {
                int updateRow = gitMixBranchMapper.updateAllMergeFlag(req.getMixBranchId(), 0, req.getOperator());
                // 自动合并失败，则返回手动集成shell
                return GitMixBranchConverter.toAddRespVo(mergeBranchResultDTO);
            }

            gitMixBranchItemMapper.updateMergeFlag(req.getMixBranchId(), branchName, 1, req.getOperator());
        }

        int updateRow = gitMixBranchMapper.updateAllMergeFlag(req.getMixBranchId(), 1, req.getOperator());
        return new AddIntoMixBranchRespVO(req.getMixBranchId(), true, null);
    }

    @Override
    public RemoveFromMixBranchRespVO removeFromMixBranch(RemoveFromMixBranchReqVO req) {
        // 校验入参
        if (null == req || null == req.getMixBranchId()) {
            throw new BizException("未找到中间分支，请联系管理员");
        }
        req.setOperator(StrUtil.trim(req.getOperator()));
        if (StrUtil.isEmptyIfStr(req.getOperator())) {
            throw new BizException("操作人不能为空");
        }
        if (CollUtil.isEmpty(req.getGitBranchNameList())) {
            throw new BizException("请选择要移除的分支");
        }

        GitMixBranchDO mixBranchDb = gitMixBranchMapper.queryByMixBranchId(req.getMixBranchId());
        if (null == mixBranchDb) {
            throw new BizException("未找到中间分支，数据库不存在 " + req.getMixBranchId());
        }

        GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectId(mixBranchDb.getProjectId());
        if (null == gitProjectDb) {
            throw new BizException("未找到git工程，数据库不存在 " + mixBranchDb.getProjectId());
        }

        // 当前集成分支
        List<GitMixBranchItemDO> mixBranchItemList = gitMixBranchItemMapper.queryByMixBranchId(req.getMixBranchId());
        if (CollUtil.isEmpty(mixBranchItemList)) {
            throw new BizException("未找到当前集成分支，数据库不存在 " + req.getMixBranchId());
        }
        List<String> mixedBranchNameList = mixBranchItemList.stream().map(GitMixBranchItemDO::getBranchName).collect(Collectors.toList());
        if (!CollUtil.containsAll(mixedBranchNameList, req.getGitBranchNameList())) {
            throw new BizException("要移除的分支范围有误，请联系管理员");
        }

        // 新的集成分支名list = 差集(已集成的分支 - 移除的分支)
        List<String> newMixBranchNameList = CollUtil.subtractToList(mixedBranchNameList, req.getGitBranchNameList());

        // 拉取所有远程分支，并且upsert到db
        fetchOriginalBranchList(gitProjectDb, gitProjectDb.getRepositoryId());

        // 重建git中间分支
        Long newMixBranchId = recreateMixBranch(mixBranchDb.getProjectId(), mixBranchDb.getMixBranchName(),
                EnvEnum.getByEnvCode(mixBranchDb.getEnv()), newMixBranchNameList, req.getOperator());

        // 重新集成
        RemergeMixBranchReqVO remergeMixBranchReqVO = new RemergeMixBranchReqVO();
        remergeMixBranchReqVO.setMixBranchId(newMixBranchId);
        remergeMixBranchReqVO.setOperator(req.getOperator());
        RemergeMixBranchRespVO remergeMixBranchRespVO = remergeMixBranch(remergeMixBranchReqVO);

        // 返回结果
        return GitMixBranchConverter.toRemoveRespVo(remergeMixBranchRespVO);
    }

    @Override
    public DeleteGitBranchRespVO deleteGitBranch(DeleteGitBranchReqVO req) {
        // 校验入参
        if (null == req) {
            throw new BizException("参数错误");
        }
        req.setOperator(StrUtil.trim(req.getOperator()));
        if (StrUtil.isEmptyIfStr(req.getOperator())) {
            throw new BizException("操作人不能为空");
        }
        if (CollUtil.isEmpty(req.getGitBranchNameList())) {
            throw new BizException("请选择要删除的分支");
        }
        if (null == req.getProjectId()
                && null == req.getMixBranchId()) {
            throw new BizException("工程id、中间分支id不能同时为空");
        }

        GitProjectDO gitProjectDb = null;
        // 根据projectId，查询git工程
        if (null != req.getProjectId()) {
            gitProjectDb = gitProjectMapper.queryByProjectId(req.getProjectId());
            if (null == gitProjectDb) {
                throw new BizException("未找到git工程，数据库不存在 " + req.getProjectId());
            }
        }
        // 根据mixBranchId，查询中间分支，再查询git工程
        else {
            GitMixBranchDO mixBranchDb = gitMixBranchMapper.queryByMixBranchId(req.getMixBranchId());
            if (null == mixBranchDb) {
                throw new BizException("未找到中间分支，数据库不存在 " + req.getMixBranchId());
            }
            gitProjectDb = gitProjectMapper.queryByProjectId(mixBranchDb.getProjectId());
            if (null == gitProjectDb) {
                throw new BizException("未找到git工程，数据库不存在 " + mixBranchDb.getProjectId());
            }
        }

        if (req.getGitBranchNameList().contains(gitProjectDb.getDefaultBranch())) {
            throw new BizException("不能直接删除主分支，请联系管理员 " + gitProjectDb.getDefaultBranch());
        }

        // 成功删除的分支名list
        List<String> successDelBranchNameList = Lists.newArrayList();
        for (String branchName : req.getGitBranchNameList()) {
            DeleteBranchReq deleteBranchReq = new DeleteBranchReq();
            setGitMergeFlowConfig(deleteBranchReq, gitProjectDb.getOrganizationId());
            deleteBranchReq.setRepositoryId(gitProjectDb.getRepositoryId());
            deleteBranchReq.setBranchName(branchName);
            DeleteBranchResp deleteBranchResp = null;
            try {
                // 调用 open api，删除分支
                deleteBranchResp = gitOpenApiFactory.deleteBranch(deleteBranchReq);
            } catch (Exception e) {
                continue;
            }
            // 删除成功
            if (null != deleteBranchResp
                    && StrUtil.isNotEmpty(deleteBranchResp.getBranchName())
                    && StrUtil.equalsIgnoreCase(branchName, deleteBranchResp.getBranchName())) {
                successDelBranchNameList.add(branchName);
            }
        }

        // 落库
        if (CollUtil.isNotEmpty(successDelBranchNameList)) {
            gitBranchMapper.deleteByProjectIdAndBranchNameList(gitProjectDb.getProjectId(), successDelBranchNameList, req.getOperator());
        }

        if (ObjUtil.notEqual(req.getGitBranchNameList().size(), successDelBranchNameList.size())) {
            log.warn("deleteGitBranch successDelBranchNameList:{}", JSON.toJSONString(successDelBranchNameList));
            throw new BizException("有分支清理失败");
        }

        return new DeleteGitBranchRespVO(successDelBranchNameList);
    }

    /**
     * 拉取所有远程分支，并且upsert到db
     */
    private void fetchOriginalBranchList(GitProjectDO gitProjectDb, Long repositoryId) {
        // 拉取所有远程分支
        ListBranchesReq listBranchesReq = new ListBranchesReq();
        setGitMergeFlowConfig(listBranchesReq, gitProjectDb.getOrganizationId());
        listBranchesReq.setRepositoryId(repositoryId);
        ListBranchesResp listBranchesResp = gitOpenApiFactory.listBranches(listBranchesReq);
        List<GitBranchDO> gitBranchDOList = buildGitBranchList(listBranchesResp, gitProjectDb.getProjectId());

        // git分支 insert or update
        if (CollUtil.isNotEmpty(gitBranchDOList)) {
            for (GitBranchDO gitBranchDO : gitBranchDOList) {
                gitBranchMapper.insertOrUpdateByBranchName(gitBranchDO);
            }
        }
    }

    /**
     * 重建git中间分支
     * <pre>
     *     除旧迎新
     *     以 test_mix 为例子
     *
     *     1、除旧
     *     已有中间分支 test_mix，则
     *          创建备份分支 backupBranchName: 250820181011_xim_tset
     *          删除远程分支 test_mix
     *          落库：记录backupBranchName、删除中间分支test_mix、删除中间分支item
     *
     *     2、迎新
     *     基于主分支，创建新的中间分支 test_mix
     *     落库：insert mix_branch、mix_branch_item
     *
     * </pre>
     *
     * @param projectId            工程id
     * @param mixBranchName        中间分支名称
     * @param envEnum              环境枚举
     * @param newMixBranchNameList 新的集成分支名称list
     * @param operator             操作人
     * @return newMixBranchId 新的中间分支id
     */
    private Long recreateMixBranch(Long projectId,
                                   String mixBranchName,
                                   EnvEnum envEnum,
                                   List<String> newMixBranchNameList,
                                   String operator) {
        GitProjectDO gitProjectDb = gitProjectMapper.queryByProjectId(projectId);
        if (null == gitProjectDb) {
            throw new BizException("未找到git工程，数据库不存在 " + projectId);
        }

        // 是否需要备份中间分支
        boolean needBackupBranch = false;
        // 备份中间分支DO
        GitBranchDO backupBranchDO = null;
        GitMixBranchDO mixBranchDb = gitMixBranchMapper.queryByProjectIdAndEnv(projectId, envEnum.getCode());
        // 本地db不存在该分支名
        if (null == mixBranchDb) {
            // 是否存在远程分支
            boolean existFlag = existOriginalBranch(gitProjectDb.getRepositoryId(), gitProjectDb.getOrganizationId(), mixBranchName);
            if (existFlag) {
                needBackupBranch = true;
            }
        }
        // 本地db已存在该分支名
        else {
            needBackupBranch = true;
        }
        // 备份分支名
        String backupBranchName = getBackupBranchName(mixBranchName);
        if (needBackupBranch) {
            // 基于当前中间分支，创建备份分支
            CreateBranchResp createBackupBranchResp = createNewBranch(gitProjectDb.getRepositoryId(), gitProjectDb.getOrganizationId(), mixBranchName, backupBranchName);
            // 备份中间分支DO
            CreateGitBranchReqVO createGitBranchReqVO = new CreateGitBranchReqVO();
            createGitBranchReqVO.setGitBranchName(backupBranchName);
            createGitBranchReqVO.setGitProjectId(gitProjectDb.getProjectId());
            createGitBranchReqVO.setBranchDesc("基于当前中间分支，创建备份分支");
            createGitBranchReqVO.setSourceBranch(mixBranchName);
            createGitBranchReqVO.setOperator(operator);
            backupBranchDO = buildGitBranchDO(createGitBranchReqVO, createBackupBranchResp);

            // 删除旧中间分支
            DeleteBranchReq deleteBranchReq = new DeleteBranchReq();
            setGitMergeFlowConfig(deleteBranchReq, gitProjectDb.getOrganizationId());
            deleteBranchReq.setRepositoryId(gitProjectDb.getRepositoryId());
            deleteBranchReq.setBranchName(mixBranchName);
            DeleteBranchResp deleteBranchResp = null;
            try {
                deleteBranchResp = gitOpenApiFactory.deleteBranch(deleteBranchReq);
            } catch (Exception e) {
                throw new BizException("删除中间分支失败:" + mixBranchName);
            }
            if (null == deleteBranchResp || StrUtil.isEmptyIfStr(deleteBranchResp.getBranchName())) {
                throw new BizException("删除中间分支失败:" + mixBranchName);
            }
        }

        // 旧的中间分支id
        Long oldMixBranchId = null != mixBranchDb ? mixBranchDb.getMixBranchId() : null;

        GitMixBranchDTO gitMixBranchDTO = buildGitMixBranchDTO(mixBranchName, envEnum, gitProjectDb.getDefaultBranch(), newMixBranchNameList, operator);
        gitMixBranchDTO.setProjectId(gitProjectDb.getProjectId());
        GitMixBranchDO gitMixBranchDO = GitMixBranchConverter.toDO(gitMixBranchDTO);

        // 基于主分支，创建新的中间分支
        CreateBranchResp createBranchResp = createNewBranch(gitProjectDb.getRepositoryId(), gitProjectDb.getOrganizationId(), gitProjectDb.getDefaultBranch(), mixBranchName);

        // 落库
        GitBranchDO finalBackupBranchDO = backupBranchDO;
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 处理旧数据
                if (null != oldMixBranchId) {
                    // update mix branch ext
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("backupBranchName", backupBranchName); // 备份分支名
                    String ext = JSON.toJSONString(jsonObject);
                    int updateRow = gitMixBranchMapper.updateExt(oldMixBranchId, ext);

                    // delete mix branch
                    int delRow = gitMixBranchMapper.deleteByMixBranchId(oldMixBranchId);

                    // delete mix branch item
                    int delItemRow = gitMixBranchItemMapper.deleteByMixBranchId(oldMixBranchId);
                }

                // insert git branch
                if (null != finalBackupBranchDO) {
                    gitBranchMapper.insertList(Lists.newArrayList(finalBackupBranchDO));
                }

                // insert git mix branch
                int mixBranchRow = gitMixBranchMapper.insertList(Lists.newArrayList(gitMixBranchDO));
                if (mixBranchRow <= 0) {
                    throw new RuntimeException("落库失败:gitMixBranch");
                }

                // insert git mix branch item
                for (GitMixBranchItemDO gitMixBranchItemDO : gitMixBranchDTO.getMixBranchItemList()) {
                    gitMixBranchItemDO.setMixBranchId(gitMixBranchDO.getMixBranchId());
                }
                int mixBranchItemRow = gitMixBranchItemMapper.insertList(gitMixBranchDTO.getMixBranchItemList());
                if (mixBranchItemRow != gitMixBranchDTO.getMixBranchItemList().size()) {
                    throw new RuntimeException("落库失败:gitMixBranchItem");
                }

                return null;
            }
        });

        return gitMixBranchDO.getMixBranchId();
    }

    /**
     * 创建新分支
     *
     * @param repositoryId     git仓库id
     * @param organizationId   git组织id
     * @param sourceBranchName 源分支名称
     * @param newBranchName    新分支名称
     */
    private CreateBranchResp createNewBranch(Long repositoryId,
                                             String organizationId,
                                             String sourceBranchName, String newBranchName) {
        CreateBranchReq createBranchReq = new CreateBranchReq();
        setGitMergeFlowConfig(createBranchReq, organizationId);
        createBranchReq.setRepositoryId(repositoryId);
        createBranchReq.setBranch(newBranchName); // 创建的分支名称
        createBranchReq.setRef(sourceBranchName); // 来源分支名称
        CreateBranchResp createBranchResp = gitOpenApiFactory.createBranch(createBranchReq);
        if (null == createBranchResp || StrUtil.isEmptyIfStr(createBranchResp.getName())) {
            throw new BizException("创建新分支失败:" + newBranchName);
        }

        return createBranchResp;
    }

    /**
     * 是否存在远程分支
     *
     * @param repositoryId 仓库id
     * @param branchName   分支名称
     * @return true代表存在，false代表不存在
     */
    private boolean existOriginalBranch(Long repositoryId, String organizationId, String branchName) {
        GetBranchReq getBranchReq = new GetBranchReq();
        setGitMergeFlowConfig(getBranchReq, organizationId);
        getBranchReq.setRepositoryId(repositoryId);
        getBranchReq.setBranchName(branchName);
        GetBranchResp getBranchResp;
        try {
            getBranchResp = gitOpenApiFactory.getBranch(getBranchReq);
        } catch (Exception e) {
            return false;
        }

        return null != getBranchResp && StrUtil.isNotEmpty(getBranchResp.getName());
    }

    /**
     * 获取备份分支名称
     * <pre>
     *     backupBranchName = yyyyMMddHHmmss_gitbackup_倒序(branchName)
     *     倒序的目的是为了避免搜索中间分支名时出现多个，需要再次手动选择，最好达到【一搜即中】的效果。
     *
     *     eg:
     *     branchName: test_mix
     *     return: 250820181011_gitbackup_xim_tset
     * </pre>
     *
     * @param branchName
     * @return
     */
    private String getBackupBranchName(String branchName) {
        return DateUtil.format(new Date(), "yyMMddHHmmss") + "_gitbackup_" + StrUtil.reverse(branchName);
    }

    /**
     * 合并源分支到目标分支
     *
     * @param mixBranchId      中间分支ID
     * @param repositoryId     仓库ID
     * @param sourceBranchName 源分支名称
     * @param targetBranchName 目标分支名称
     * @param operator         操作人
     * @return
     */
    private DoMergeBranchResultDTO doMergeIntoMixBranch(Long mixBranchId,
                                                        Long repositoryId,
                                                        String organizationId,
                                                        String sourceBranchName,
                                                        String targetBranchName,
                                                        String operator) {
        String titleTemplate = GitConstant.SYSTEM_NAME + ": merge branch %s into %s";
        String title = String.format(titleTemplate, sourceBranchName, targetBranchName);

        CreateChangeRequestReq createChangeRequestReq = new CreateChangeRequestReq();
        setGitMergeFlowConfig(createChangeRequestReq, organizationId);
        createChangeRequestReq.setRepositoryId(repositoryId);
        createChangeRequestReq.setSourceBranch(sourceBranchName);
        createChangeRequestReq.setSourceProjectId(repositoryId);
        createChangeRequestReq.setTargetBranch(targetBranchName);
        createChangeRequestReq.setTargetProjectId(repositoryId);
        createChangeRequestReq.setTitle(title);
        createChangeRequestReq.setDescription(title);

        GitMixBranchDO gitMixBranchDO = gitMixBranchMapper.queryByMixBranchId(mixBranchId);

        CreateChangeRequestResp createChangeRequestResp = null;
        if (null == gitMixBranchDO.getMergeRequestId()) {
            // 调用 open api，创建合并请求
            createChangeRequestResp = gitOpenApiFactory.createChangeRequest(createChangeRequestReq);
        }
        // 存在进行中的合并请求，则mock createChangeRequestResp
        else {
            createChangeRequestResp = new CreateChangeRequestResp();
            createChangeRequestResp.setLocalId(gitMixBranchDO.getMergeRequestId());
            createChangeRequestResp.setMergeTotalStatus(MergeTotalStatusEnum.CHECKING.getStatus());
        }
        if (Objects.equals(MergeTotalStatusEnum.MERGED.getStatus(), createChangeRequestResp.getMergeTotalStatus())) {
            // 已合并
            gitMixBranchMapper.updateMergeRequestId(mixBranchId, null);
            gitMixBranchItemMapper.updateMergeFlag(mixBranchId, sourceBranchName, 1, operator);
            return new DoMergeBranchResultDTO(mixBranchId, true, null);
        }

        // 可以进行合并
        if (Objects.equals(MergeTotalStatusEnum.CAN_BE_MERGED.getStatus(), createChangeRequestResp.getMergeTotalStatus())) {
            // 调用 open api，合并合并请求
            MergeChangeRequestReq mergeChangeRequestReq = new MergeChangeRequestReq();
            setGitMergeFlowConfig(mergeChangeRequestReq, organizationId);
            mergeChangeRequestReq.setRepositoryId(repositoryId);
            mergeChangeRequestReq.setLocalId(createChangeRequestResp.getLocalId());
            MergeChangeRequestResp mergeChangeRequestResp = gitOpenApiFactory.mergeChangeRequest(mergeChangeRequestReq);
            if (Objects.equals(MergeTotalStatusEnum.MERGED.getStatus(), mergeChangeRequestResp.getMergeTotalStatus())) {
                // 已合并
                gitMixBranchMapper.updateMergeRequestId(mixBranchId, null);
                gitMixBranchItemMapper.updateMergeFlag(mixBranchId, sourceBranchName, 1, operator);
                return new DoMergeBranchResultDTO(mixBranchId, true, null);
            }
        }

        GetChangeRequestReq getChangeRequestReq = new GetChangeRequestReq();
        setGitMergeFlowConfig(getChangeRequestReq, organizationId);
        getChangeRequestReq.setRepositoryId(repositoryId);
        getChangeRequestReq.setLocalId(createChangeRequestResp.getLocalId());

        boolean canMergeFlag = false;
        for (int i = 0; i < 3; i++) {
            // 调用 open api，查询合并请求
            GetChangeRequestResp getChangeRequestResp = gitOpenApiFactory.getChangeRequest(getChangeRequestReq);

            if (Objects.equals(MergeTotalStatusEnum.MERGED.getStatus(), getChangeRequestResp.getMergeTotalStatus())) {
                // 已合并
                gitMixBranchMapper.updateMergeRequestId(mixBranchId, null);
                gitMixBranchItemMapper.updateMergeFlag(mixBranchId, sourceBranchName, 1, operator);
                return new DoMergeBranchResultDTO(mixBranchId, true, null);
            }

            // 无法合并
            if (Objects.equals(MergeTotalStatusEnum.CAN_NOT_BE_MERGED.getStatus(), getChangeRequestResp.getMergeTotalStatus())) {
                break;
            }

            // 可以合并
            if (Objects.equals(MergeTotalStatusEnum.CAN_BE_MERGED.getStatus(), getChangeRequestResp.getMergeTotalStatus())) {
                canMergeFlag = true;
                break;
            }

            // 检测中，则等待2秒后重试
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (canMergeFlag) {
            // 调用 open api，合并合并请求
            MergeChangeRequestReq mergeChangeRequestReq = new MergeChangeRequestReq();
            setGitMergeFlowConfig(mergeChangeRequestReq, organizationId);
            mergeChangeRequestReq.setRepositoryId(repositoryId);
            mergeChangeRequestReq.setLocalId(createChangeRequestResp.getLocalId());
            MergeChangeRequestResp mergeChangeRequestResp = gitOpenApiFactory.mergeChangeRequest(mergeChangeRequestReq);
            if (Objects.equals(MergeTotalStatusEnum.MERGED.getStatus(), mergeChangeRequestResp.getMergeTotalStatus())) {
                // 已合并
                gitMixBranchMapper.updateMergeRequestId(mixBranchId, null);
                gitMixBranchItemMapper.updateMergeFlag(mixBranchId, sourceBranchName, 1, operator);
                return new DoMergeBranchResultDTO(mixBranchId, true, null);
            }
        }

        // 需要用户手动处理冲突
        gitMixBranchMapper.updateMergeRequestId(mixBranchId, createChangeRequestResp.getLocalId()); // set （git服务平台的）合并请求的id
        gitMixBranchItemMapper.updateMergeFlag(mixBranchId, sourceBranchName, 0, operator);
        return new DoMergeBranchResultDTO(mixBranchId, false, getUserMergeShell(sourceBranchName, targetBranchName));
    }

    private String getUserMergeShell(String sourceBranchName, String targetBranchName) {
        String shellTemplate = "自动集成失败，请手动处理冲突，再重新集成\n" +
                "git fetch origin; git checkout -b %s origin/%s; git checkout %s; git pull\n" +
                "git merge origin/%s";
        return String.format(shellTemplate, targetBranchName, targetBranchName, targetBranchName, sourceBranchName);
    }

    private GitBranchDO buildGitBranchDO(CreateGitBranchReqVO req, CreateBranchResp createBranchResp) {
        GitBranchDO gitBranchDO = new GitBranchDO();
        // gitBranchDO.setBranchId(); // auto-incr
        gitBranchDO.setBranchName(req.getGitBranchName());
        gitBranchDO.setProjectId(req.getGitProjectId());
        gitBranchDO.setBranchDesc(req.getBranchDesc());
        gitBranchDO.setMergedMasterFlag(0);
        gitBranchDO.setDefaultBranchFlag(0);
        gitBranchDO.setSourceBranch(req.getSourceBranch());
        gitBranchDO.setCreateBy(req.getOperator());
        gitBranchDO.setCreateTime(new Date());
        gitBranchDO.setUpdateBy(req.getOperator());
        gitBranchDO.setUpdateTime(new Date());
        gitBranchDO.setLastCommitTime(createBranchResp.getLastCommitTime());
        gitBranchDO.setLastCommitUser(createBranchResp.getLastCommitUser());
        gitBranchDO.setLastCommitEmail(createBranchResp.getLastCommitEmail());
        gitBranchDO.setLastCommitId(createBranchResp.getLastCommitId());
        gitBranchDO.setLastCommitShortId(createBranchResp.getLastCommitShortId());
        gitBranchDO.setLastCommitMessage(createBranchResp.getLastCommitMessage());
        gitBranchDO.setDeleted(0L);
        gitBranchDO.setBranchExt(null);
        return gitBranchDO;
    }

    private List<GitBranchDO> buildGitBranchList(ListBranchesResp listBranchesResp,
                                                 Long projectId) {
        if (null == listBranchesResp || CollUtil.isEmpty(listBranchesResp.getBranchItemList())) {
            return Collections.emptyList();
        }

        List<GitBranchDO> gitBranchDOList = Lists.newArrayList();
        for (ListBranchesResp.BranchItem branchItem : listBranchesResp.getBranchItemList()) {
            GitBranchDO gitBranchDO = new GitBranchDO();
            // gitBranchDO.setBranchId(); // auto-incr
            gitBranchDO.setBranchName(branchItem.getName());
            gitBranchDO.setProjectId(projectId);
            gitBranchDO.setBranchDesc(branchItem.getName()); // 后期创建分支时，需要按具体的需求名来填写
            gitBranchDO.setDefaultBranchFlag(branchItem.getDefaultBranch() ? 1 : 0);
            gitBranchDO.setMergedMasterFlag(0);
            gitBranchDO.setSourceBranch("master");
            gitBranchDO.setCreateBy(branchItem.getAuthorName());
            gitBranchDO.setCreateTime(branchItem.getCommittedDate());
            gitBranchDO.setUpdateBy(branchItem.getAuthorName());
            gitBranchDO.setUpdateTime(branchItem.getLastCommitTime());
            gitBranchDO.setLastCommitTime(branchItem.getLastCommitTime());
            gitBranchDO.setLastCommitUser(branchItem.getLastCommitUser());
            gitBranchDO.setLastCommitEmail(branchItem.getLastCommitEmail());
            gitBranchDO.setLastCommitId(branchItem.getLastCommitId());
            gitBranchDO.setLastCommitShortId(branchItem.getLastCommitShortId());
            gitBranchDO.setLastCommitMessage(branchItem.getLastCommitMessage());
            gitBranchDO.setDeleted(0L);
            gitBranchDO.setBranchExt(null);
            gitBranchDOList.add(gitBranchDO);
        }
        return gitBranchDOList;
    }

    private GitProjectDO buildGitProject(GetRepositoryResp repository,
                                         String organizationId,
                                         String operator) {
        GitProjectDO gitProjectDO = new GitProjectDO();
        // gitProjectDO.setProjectId(); // auto-incr
        gitProjectDO.setProjectName(repository.getName());
        gitProjectDO.setRepositoryUrl(repository.getHttpUrlToRepo());
        gitProjectDO.setOrganizationId(organizationId);
        gitProjectDO.setRepositoryId(repository.getId());
        // gitProjectDO.setProjectUsername(repository.get);
        // gitProjectDO.setProjectEmail();
        gitProjectDO.setDefaultBranch(repository.getDefaultBranch());
        gitProjectDO.setWebUrl(repository.getWebUrl());
        gitProjectDO.setCreateBy(operator);
        gitProjectDO.setCreateTime(new Date());
        gitProjectDO.setUpdateBy(operator);
        gitProjectDO.setUpdateTime(new Date());
        gitProjectDO.setDeleted(0L);
        return gitProjectDO;
    }

}