package com.feeltens.git.schedule;

import cn.hutool.core.collection.CollUtil;
import com.feeltens.git.service.GitFlowService;
import com.feeltens.git.vo.req.PullRemoteBranchReqVO;
import com.feeltens.git.vo.resp.PageGitProjectRespVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 拉取远程分支 task
 */
@Component
@RequiredArgsConstructor
public class PullRemoteBranchTask {

    private final GitFlowService gitFlowService;

    @Async
    @Scheduled(cron = "0 */15 * * * *") // 每隔15min执行一次
    public void pullRemoteBranch() {
        List<PageGitProjectRespVO> projectList = gitFlowService.listGitProject();
        if (CollUtil.isEmpty(projectList)) {
            return;
        }

        for (PageGitProjectRespVO project : projectList) {
            gitFlowService.pullRemoteBranch(PullRemoteBranchReqVO.builder().gitProjectId(project.getProjectId()).build());
        }
    }

}