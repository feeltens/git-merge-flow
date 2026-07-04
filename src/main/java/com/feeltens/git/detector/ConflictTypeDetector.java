package com.feeltens.git.detector;

import com.feeltens.git.dto.conflict.ConflictType;
import com.feeltens.git.dto.conflict.GitIndexEntry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 冲突类型检测器
 * 根据Git索引的stage信息检测冲突类型
 *
 * @author feeltens
 */
@Component
@Slf4j
public class ConflictTypeDetector {

    /**
     * 检测冲突类型
     *
     * @param indexMap 索引信息map（stage -> entry）
     * @return 冲突类型
     */
    public ConflictType detectConflictType(Map<Integer, GitIndexEntry> indexMap) {
        boolean hasBase = indexMap.containsKey(1);
        boolean hasOurs = indexMap.containsKey(2);
        boolean hasTheirs = indexMap.containsKey(3);

        log.debug("检测冲突类型: hasBase={}, hasOurs={}, hasTheirs={}",
                hasBase, hasOurs, hasTheirs);

        // 双方都添加了文件（ADDED_ADDED）
        if (!hasBase && hasOurs && hasTheirs) {
            return ConflictType.ADDED_ADDED;
        }

        // 一方修改，一方删除（MODIFIED_DELETED）
        if (hasBase && hasOurs && !hasTheirs) {
            return ConflictType.MODIFIED_DELETED;
        }

        // 一方删除，一方修改（DELETED_MODIFIED）
        if (hasBase && !hasOurs && hasTheirs) {
            return ConflictType.DELETED_MODIFIED;
        }

        // 双方都删除了文件（DELETED_DELETED）
        if (hasBase && !hasOurs && !hasTheirs) {
            return ConflictType.DELETED_DELETED;
        }

        // 文件被添加（ADDED）
        if (!hasBase && hasOurs && !hasTheirs) {
            return ConflictType.ADDED;
        }

        // 文件被删除（DELETED）
        if (hasBase && !hasOurs && !hasTheirs) {
            return ConflictType.DELETED;
        }

        // 标准冲突（双方修改）
        if (hasBase && hasOurs && hasTheirs) {
            return ConflictType.DEFAULT;
        }

        // 未知类型
        return ConflictType.UNKNOWN;
    }

    /**
     * 检测是否为rebase冲突
     *
     * @param repository Git仓库
     * @return 是否为rebase冲突
     */
    public boolean isRebaseConflict(Repository repository) {
        try {
            // 检查是否存在REBASE_HEAD文件
            java.io.File rebaseHead = new java.io.File(
                    repository.getDirectory(), "REBASE_HEAD");
            return rebaseHead.exists();
        } catch (Exception e) {
            log.warn("检测rebase状态失败", e);
            return false;
        }
    }

}