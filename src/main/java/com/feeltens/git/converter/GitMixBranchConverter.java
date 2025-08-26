package com.feeltens.git.converter;

import cn.hutool.core.collection.CollUtil;
import com.feeltens.git.dto.DoMergeBranchResultDTO;
import com.feeltens.git.dto.GitMixBranchDTO;
import com.feeltens.git.entity.GitMixBranchDO;
import com.feeltens.git.vo.resp.AddIntoMixBranchRespVO;
import com.feeltens.git.vo.resp.PageMixBranchRespVO;
import com.feeltens.git.vo.resp.RemergeMixBranchRespVO;
import com.feeltens.git.vo.resp.RemoveFromMixBranchRespVO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GitMixBranchConverter {

    public static List<PageMixBranchRespVO> toPageVos(List<GitMixBranchDO> reqList) {
        if (CollUtil.isEmpty(reqList)) {
            return Collections.emptyList();
        }

        return reqList.stream().map(item -> PageMixBranchRespVO.builder()
                .mixBranchId(item.getMixBranchId())
                .mixBranchName(item.getMixBranchName())
                .env(item.getEnv())
                .projectId(item.getProjectId())
                .allMergeFlag(item.getAllMergeFlag())
                .createBy(item.getCreateBy())
                .createTime(item.getCreateTime())
                .updateBy(item.getUpdateBy())
                .updateTime(item.getUpdateTime())
                .build()
        ).collect(Collectors.toList());
    }

    public static GitMixBranchDO toDO(GitMixBranchDTO req) {
        if (null == req) {
            return null;
        }

        return GitMixBranchDO.builder()
                .mixBranchId(req.getMixBranchId())
                .mixBranchName(req.getMixBranchName())
                .env(req.getEnv())
                .projectId(req.getProjectId())
                .allMergeFlag(req.getAllMergeFlag())
                .mergeRequestId(null)
                .createBy(req.getCreateBy())
                .createTime(req.getCreateTime())
                .updateBy(req.getUpdateBy())
                .updateTime(req.getUpdateTime())
                .deleted(req.getDeleted())
                .mixBranchExt(req.getMixBranchExt())
                .build();
    }

    public static RemergeMixBranchRespVO toRespVo(DoMergeBranchResultDTO req) {
        return RemergeMixBranchRespVO.builder()
                .mixBranchId(req.getMixBranchId())
                .flag(req.getFlag())
                .userMergeShell(req.getUserMergeShell())
                .build();
    }

    public static AddIntoMixBranchRespVO toAddRespVo(DoMergeBranchResultDTO req) {
        return AddIntoMixBranchRespVO.builder()
                .mixBranchId(req.getMixBranchId())
                .flag(req.getFlag())
                .userMergeShell(req.getUserMergeShell())
                .build();
    }

    public static RemoveFromMixBranchRespVO toRemoveRespVo(RemergeMixBranchRespVO req) {
        return RemoveFromMixBranchRespVO.builder()
                .mixBranchId(req.getMixBranchId())
                .flag(req.getFlag())
                .userMergeShell(req.getUserMergeShell())
                .build();
    }

}