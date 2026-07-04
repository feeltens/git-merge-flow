package com.feeltens.git.service.impl;

import com.feeltens.git.diff.DiffCache;
import com.feeltens.git.diff.DiffCalculator;
import com.feeltens.git.dto.conflict.ConflictFileContent;
import com.feeltens.git.dto.conflict.ConflictSession;
import com.feeltens.git.dto.diff.ThreeWayDiffVO;
import com.feeltens.git.service.ConflictSessionService;
import com.feeltens.git.service.DiffRenderService;
import com.feeltens.git.service.JGitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;

/**
 * Diff渲染服务实现
 *
 * @author feeltens
 */
@Service
@Slf4j
public class DiffRenderServiceImpl implements DiffRenderService {

    @Resource
    private JGitService jGitService;

    @Resource
    private DiffCalculator diffCalculator;

    @Resource
    private DiffCache diffCache;

    @Resource
    private ConflictSessionService conflictSessionService;

    @Override
    public ThreeWayDiffVO calculateThreeWayDiff(String sessionId, String filePath) {
        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(sessionId, filePath);

            // 尝试从缓存获取
            ThreeWayDiffVO cached = diffCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 获取本地仓库路径
            String localRepoPath = getLocalRepoPath(sessionId);

            // 使用JGitService读取冲突文件内容
            ConflictFileContent content = jGitService.getConflictFileContent(localRepoPath, filePath);

            // 使用DiffCalculator计算三路diff
            ThreeWayDiffVO result = diffCalculator.calculateThreeWayDiff(
                    content.getBaseContent(),
                    content.getOursContent(),
                    content.getTheirsContent(),
                    filePath
            );

            // 缓存结果
            diffCache.put(cacheKey, result);

            return result;
        } catch (Exception e) {
            log.error("计算三路diff失败: sessionId={}, filePath={}", sessionId, filePath, e);
            throw new RuntimeException("计算三路diff失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成缓存键
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return 缓存键
     */
    private String generateCacheKey(String sessionId, String filePath) {
        try {
            String key = sessionId + ":" + filePath;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("生成缓存键失败，使用简单键", e);
            return sessionId + ":" + filePath;
        }
    }

    /**
     * 获取本地仓库路径
     *
     * @param sessionId 会话ID
     * @return 本地仓库路径
     */
    private String getLocalRepoPath(String sessionId) {
        ConflictSession session = conflictSessionService.getSession(sessionId);
        if (session == null) {
            log.error("会话不存在: sessionId={}", sessionId);
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        String localRepoPath = session.getLocalRepoPath();
        if (localRepoPath == null || localRepoPath.isEmpty()) {
            log.error("本地仓库路径为空: sessionId={}", sessionId);
            throw new IllegalStateException("本地仓库路径为空: " + sessionId);
        }
        log.debug("获取本地仓库路径: sessionId={}, localRepoPath={}", sessionId, localRepoPath);
        return localRepoPath;
    }

}