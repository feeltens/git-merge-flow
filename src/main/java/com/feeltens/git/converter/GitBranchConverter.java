package com.feeltens.git.converter;

import cn.hutool.core.collection.CollUtil;
import com.feeltens.git.entity.GitBranchDO;
import com.feeltens.git.vo.resp.GitBranchRespVO;
import com.feeltens.git.vo.resp.ListGitBranchRespVO;
import com.feeltens.git.vo.resp.PageGitBranchRespVO;
import com.feeltens.git.vo.resp.QueryGitBranchRespVO;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GitBranchConverter {

    public static List<PageGitBranchRespVO> toPageVos(List<GitBranchDO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return Collections.emptyList();
        }

        return reqList.stream().map(item -> PageGitBranchRespVO.builder()
                .branchId(item.getBranchId())
                .branchName(item.getBranchName())
                .projectId(item.getProjectId())
                .branchDesc(item.getBranchDesc())
                .mergedMasterFlag(item.getMergedMasterFlag())
                .sourceBranch(item.getSourceBranch())
                .createBy(item.getCreateBy())
                .createTime(item.getCreateTime())
                .updateBy(item.getUpdateBy())
                .updateTime(item.getUpdateTime())
                .lastCommitTime(item.getLastCommitTime())
                .lastCommitUser(item.getLastCommitUser())
                .lastCommitEmail(item.getLastCommitEmail())
                .lastCommitId(item.getLastCommitId())
                .lastCommitShortId(item.getLastCommitShortId())
                .lastCommitMessage(item.getLastCommitMessage())
                .build()
        ).collect(Collectors.toList());
    }

    public static List<GitBranchRespVO> convertToMixBranchVoList(List<GitBranchDO> toMixBranchList) {
        if (CollUtil.isEmpty(toMixBranchList)) {
            return Collections.emptyList();
        }

        List<GitBranchRespVO> resultList = Lists.newArrayList();
        for (GitBranchDO gitBranchDO : toMixBranchList) {
            GitBranchRespVO vo = new GitBranchRespVO();
            vo.setBranchId(gitBranchDO.getBranchId());
            vo.setBranchName(gitBranchDO.getBranchName());
            vo.setProjectId(gitBranchDO.getProjectId());
            vo.setBranchDesc(gitBranchDO.getBranchDesc());
            // vo.setMergedMasterFlag();
            vo.setSourceBranch(gitBranchDO.getSourceBranch());
            vo.setCreateBy(gitBranchDO.getCreateBy());
            vo.setCreateTime(gitBranchDO.getCreateTime());
            vo.setUpdateBy(gitBranchDO.getUpdateBy());
            vo.setUpdateTime(gitBranchDO.getUpdateTime());
            vo.setLastCommitTime(gitBranchDO.getLastCommitTime());
            vo.setLastCommitUser(gitBranchDO.getLastCommitUser());
            vo.setLastCommitEmail(gitBranchDO.getLastCommitEmail());
            vo.setLastCommitId(gitBranchDO.getLastCommitId());
            vo.setLastCommitShortId(gitBranchDO.getLastCommitShortId());
            vo.setLastCommitMessage(gitBranchDO.getLastCommitMessage());
            vo.setMergeStatus(-1); // -1 未集成

            resultList.add(vo);
        }
        return resultList;
    }

    public static List<GitBranchRespVO> convertMixedBranchVoList(List<GitBranchDO> mixedBranchList,
                                                                 Map<String, Integer> mixBranchName2MergeFlagMap) {
        if (CollUtil.isEmpty(mixedBranchList)) {
            return Collections.emptyList();
        }

        List<GitBranchRespVO> resultList = Lists.newArrayList();
        for (GitBranchDO gitBranchDO : mixedBranchList) {
            GitBranchRespVO vo = new GitBranchRespVO();
            vo.setBranchId(gitBranchDO.getBranchId());
            vo.setBranchName(gitBranchDO.getBranchName());
            vo.setProjectId(gitBranchDO.getProjectId());
            vo.setBranchDesc(gitBranchDO.getBranchDesc());
            // vo.setMergedMasterFlag();
            vo.setSourceBranch(gitBranchDO.getSourceBranch());
            vo.setCreateBy(gitBranchDO.getCreateBy());
            vo.setCreateTime(gitBranchDO.getCreateTime());
            vo.setUpdateBy(gitBranchDO.getUpdateBy());
            vo.setUpdateTime(gitBranchDO.getUpdateTime());
            vo.setLastCommitTime(gitBranchDO.getLastCommitTime());
            vo.setLastCommitUser(gitBranchDO.getLastCommitUser());
            vo.setLastCommitEmail(gitBranchDO.getLastCommitEmail());
            vo.setLastCommitId(gitBranchDO.getLastCommitId());
            vo.setLastCommitShortId(gitBranchDO.getLastCommitShortId());
            vo.setLastCommitMessage(gitBranchDO.getLastCommitMessage());
            vo.setMergeStatus(mixBranchName2MergeFlagMap.get(gitBranchDO.getBranchName()));

            resultList.add(vo);
        }
        return resultList;
    }

    public static List<ListGitBranchRespVO> toListVos(List<GitBranchDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        List<ListGitBranchRespVO> resultList = Lists.newArrayList();
        for (GitBranchDO item : list) {
            ListGitBranchRespVO vo = ListGitBranchRespVO.builder()
                    .branchId(item.getBranchId())
                    .branchName(item.getBranchName())
                    .projectId(item.getProjectId())
                    .branchDesc(item.getBranchDesc())
                    .defaultBranchFlag(item.getDefaultBranchFlag())
                    .sourceBranch(item.getSourceBranch())
                    .createBy(item.getCreateBy())
                    .createTime(item.getCreateTime())
                    .updateBy(item.getUpdateBy())
                    .updateTime(item.getUpdateTime())
                    .lastCommitTime(item.getLastCommitTime())
                    .lastCommitUser(item.getLastCommitUser())
                    .lastCommitEmail(item.getLastCommitEmail())
                    .lastCommitId(item.getLastCommitId())
                    .lastCommitShortId(item.getLastCommitShortId())
                    .lastCommitMessage(item.getLastCommitMessage())
                    .mergeStatus(-1)
                    .build();
            resultList.add(vo);
        }

        return resultList;
    }

    public static QueryGitBranchRespVO toQueryRespVo(GitBranchDO gitBranchDO) {
        if (null == gitBranchDO) {
            return null;
        }

        return QueryGitBranchRespVO.builder()
                .branchId(gitBranchDO.getBranchId())
                .branchName(gitBranchDO.getBranchName())
                .projectId(gitBranchDO.getProjectId())
                .branchDesc(gitBranchDO.getBranchDesc())
                .defaultBranchFlag(gitBranchDO.getDefaultBranchFlag())
                .sourceBranch(gitBranchDO.getSourceBranch())
                .createBy(gitBranchDO.getCreateBy())
                .createTime(gitBranchDO.getCreateTime())
                .updateBy(gitBranchDO.getUpdateBy())
                .updateTime(gitBranchDO.getUpdateTime())
                .lastCommitTime(gitBranchDO.getLastCommitTime())
                .lastCommitUser(gitBranchDO.getLastCommitUser())
                .lastCommitEmail(gitBranchDO.getLastCommitEmail())
                .lastCommitId(gitBranchDO.getLastCommitId())
                .lastCommitShortId(gitBranchDO.getLastCommitShortId())
                .lastCommitMessage(gitBranchDO.getLastCommitMessage())
                .build();
    }

}