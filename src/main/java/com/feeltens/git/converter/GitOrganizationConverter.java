package com.feeltens.git.converter;

import cn.hutool.core.collection.CollUtil;
import com.feeltens.git.entity.GitOrganizationDO;
import com.feeltens.git.vo.resp.PageGitOrganizationRespVO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GitOrganizationConverter {

    public static List<PageGitOrganizationRespVO> toPageVos(List<GitOrganizationDO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return Collections.emptyList();
        }

        return reqList.stream().map(item -> PageGitOrganizationRespVO.builder()
                .orgInnerId(item.getOrgInnerId())
                .organizationId(item.getOrganizationId())
                .organizationName(item.getOrganizationName())
                .description(item.getDescription())
                .webUrl(item.getWebUrl())
                .createBy(item.getCreateBy())
                .createTime(item.getCreateTime())
                .updateBy(item.getUpdateBy())
                .updateTime(item.getUpdateTime())
                .build()
        ).collect(Collectors.toList());
    }

}