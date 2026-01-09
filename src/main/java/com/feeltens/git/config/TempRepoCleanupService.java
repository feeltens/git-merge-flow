package com.feeltens.git.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;

/**
 * 临时仓库清理服务
 * 在Spring Boot服务关闭时自动清理临时仓库目录
 *
 * @author feeltens
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TempRepoCleanupService {

    private final JGitConfig jGitConfig;

    /**
     * 服务关闭时清理临时仓库目录
     */
    @PreDestroy
    public void cleanup() {
        String tempRepoPath = jGitConfig.getTempRepoPath();
        if (tempRepoPath == null || tempRepoPath.trim().isEmpty()) {
            log.warn("临时仓库路径未配置，跳过清理");
            return;
        }

        File tempDir = new File(tempRepoPath);
        if (!tempDir.exists()) {
            log.info("临时仓库目录不存在，无需清理: {}", tempRepoPath);
            return;
        }

        log.info("开始清理临时仓库目录: {}", tempRepoPath);
        try {
            int deletedCount = deleteDirectoryContents(tempDir);
            log.info("临时仓库目录清理完成，共删除 {} 个文件/文件夹", deletedCount);
        } catch (Exception e) {
            log.error("清理临时仓库目录失败: {}", tempRepoPath, e);
        }
    }

    /**
     * 递归删除目录内容（保留根目录本身）
     *
     * @param directory 目录
     * @return 删除的文件/文件夹数量
     */
    private int deleteDirectoryContents(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files == null) {
            return count;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                count += deleteDirectoryRecursively(file);
            } else {
                if (file.delete()) {
                    count++;
                } else {
                    log.warn("删除文件失败: {}", file.getAbsolutePath());
                }
            }
        }
        return count;
    }

    /**
     * 递归删除目录及其所有内容
     *
     * @param directory 目录
     * @return 删除的文件/文件夹数量
     */
    private int deleteDirectoryRecursively(File directory) {
        int count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += deleteDirectoryRecursively(file);
                } else {
                    if (file.delete()) {
                        count++;
                    } else {
                        log.warn("删除文件失败: {}", file.getAbsolutePath());
                    }
                }
            }
        }
        if (directory.delete()) {
            count++;
        } else {
            log.warn("删除目录失败: {}", directory.getAbsolutePath());
        }
        return count;
    }

}