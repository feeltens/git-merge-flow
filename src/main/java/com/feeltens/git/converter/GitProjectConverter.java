package com.feeltens.git.converter;

import cn.hutool.core.collection.CollUtil;
import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.vo.resp.PageGitProjectRespVO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GitProjectConverter {

    public static List<PageGitProjectRespVO> toPageVos(List<GitProjectDO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return Collections.emptyList();
        }

        return reqList.stream().map(item -> PageGitProjectRespVO.builder()
                .projectId(item.getProjectId())
                .projectName(item.getProjectName())
                .repositoryUrl(item.getRepositoryUrl())
                .organizationId(item.getOrganizationId())
                .repositoryId(item.getRepositoryId())
                .projectUsername(item.getProjectUsername())
                .projectEmail(item.getProjectEmail())
                .defaultBranch(item.getDefaultBranch())
                .webUrl(item.getWebUrl())
                .createBy(item.getCreateBy())
                .createTime(item.getCreateTime())
                .updateBy(item.getUpdateBy())
                .updateTime(item.getUpdateTime())
                .build()
        ).collect(Collectors.toList());
    }

}