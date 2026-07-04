package com.feeltens.git.schedule;

import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.enums.CloneStatus;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.service.RepoCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 主分支同步定时任务
 * 定时同步所有项目的主分支代码到本地缓存
 *
 * @author feeltens
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MainBranchSyncTask {

    private final RepoCacheService repoCacheService;
    private final GitProjectMapper gitProjectMapper;

    /**
     * 定时同步主分支
     * 默认每30分钟执行一次
     */
    @Scheduled(cron = "${merge-flow.schedule.sync-main-branch}")
    public void syncMainBranch() {
        log.info("开始执行主分支同步任务");

        try {
            // 查询所有已克隆的项目
            List<GitProjectDO> projects = gitProjectMapper.selectAll();
            if (projects == null || projects.isEmpty()) {
                log.info("没有需要同步的项目");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (GitProjectDO project : projects) {
                // 只同步已克隆的项目
                if (!CloneStatus.CLONED.getCode().equals(project.getCloneStatus())) {
                    log.debug("跳过未克隆的项目: projectId={}, cloneStatus={}",
                            project.getProjectId(), project.getCloneStatus());
                    continue;
                }

                try {
                    repoCacheService.pullMainBranch(project.getProjectId());
                    successCount++;
                } catch (Exception e) {
                    log.error("同步主分支失败: projectId={}, error={}",
                            project.getProjectId(), e.getMessage(), e);
                    failCount++;
                }
            }

            log.info("主分支同步任务完成: 成功={}{}", successCount, failCount <= 0 ? "" : ", 失败=" + failCount);
        } catch (Exception e) {
            log.error("主分支同步任务执行异常: {}", e.getMessage(), e);
        }
    }

}