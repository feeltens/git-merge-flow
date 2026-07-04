package com.feeltens.git.schedule;

import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.enums.CloneStatus;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.service.RepoCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 项目克隆定时任务
 * 定时扫描并克隆未克隆的项目
 *
 * @author feeltens
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProjectCloneTask {

    private final RepoCacheService repoCacheService;
    private final GitProjectMapper gitProjectMapper;

    /**
     * 异步任务执行器
     */
    @Resource(name = "asyncTaskExecutor")
    private Executor asyncTaskExecutor;

    /**
     * 定时克隆未克隆的项目
     * 默认每5分钟执行一次
     */
    @Scheduled(cron = "${merge-flow.schedule.clone-pending-projects}")
    public void clonePendingProjects() {
        log.info("开始执行项目克隆任务");

        try {
            // 查询所有项目
            List<GitProjectDO> projects = gitProjectMapper.selectAll();
            if (projects == null || projects.isEmpty()) {
                log.info("没有需要克隆的项目");
                return;
            }

            int pendingCount = 0;
            int cloningCount = 0;
            int clonedCount = 0;

            for (GitProjectDO project : projects) {
                String cloneStatus = project.getCloneStatus();

                if (CloneStatus.NOT_CLONED.getCode().equals(cloneStatus)) {
                    // 未克隆，提交异步克隆任务
                    pendingCount++;
                    submitCloneTask(project.getProjectId());
                } else if (CloneStatus.CLONING.getCode().equals(cloneStatus)) {
                    // 克隆中，跳过
                    cloningCount++;
                } else if (CloneStatus.CLONED.getCode().equals(cloneStatus)) {
                    // 已克隆，跳过
                    clonedCount++;
                } else if (CloneStatus.CLONE_FAILED.getCode().equals(cloneStatus)) {
                    // 克隆失败，重试
                    pendingCount++;
                    submitCloneTask(project.getProjectId());
                }
            }

            log.info("项目克隆任务扫描完成: 待克隆={}, 克隆中={}, 已克隆={}",
                    pendingCount, cloningCount, clonedCount);

            // 输出线程池状态
            if (asyncTaskExecutor instanceof ThreadPoolExecutor) {
                ThreadPoolExecutor executor = (ThreadPoolExecutor) asyncTaskExecutor;
                log.info("克隆线程池状态: 活跃线程={}, 队列大小={}, 完成任务数={}",
                        executor.getActiveCount(), executor.getQueue().size(), executor.getCompletedTaskCount());
            }

        } catch (Exception e) {
            log.error("项目克隆任务执行异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 提交克隆任务到线程池
     */
    private void submitCloneTask(Long projectId) {
        try {
            asyncTaskExecutor.execute(() -> {
                try {
                    log.info("开始克隆项目: projectId={}", projectId);
                    repoCacheService.cloneFullRepo(projectId);
                    log.info("项目克隆完成: projectId={}", projectId);
                } catch (Exception e) {
                    log.error("项目克隆失败: projectId={}, error={}", projectId, e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("提交克隆任务失败: projectId={}, error={}", projectId, e.getMessage(), e);
        }
    }

}