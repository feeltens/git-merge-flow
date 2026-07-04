package com.feeltens.git.service.impl;

import cn.hutool.core.util.StrUtil;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.config.CodeupConfig;
import com.feeltens.git.config.GitLabConfig;
import com.feeltens.git.config.GitMergeFlowConfig;
import com.feeltens.git.config.JGitConfig;
import com.feeltens.git.dto.conflict.ConflictFileContent;
import com.feeltens.git.dto.conflict.ConflictSession;
import com.feeltens.git.dto.conflict.GitCredentials;
import com.feeltens.git.dto.conflict.MergeResult;
import com.feeltens.git.dto.conflict.ParsedConflict;
import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.enums.GitServiceEnum;
import com.feeltens.git.enums.InitProgressStatus;
import com.feeltens.git.enums.SessionStatus;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.oapi.dto.req.GetCompareReq;
import com.feeltens.git.oapi.dto.resp.GetCompareResp;
import com.feeltens.git.oapi.factory.GitOpenApiFactory;
import com.feeltens.git.service.ConflictParser;
import com.feeltens.git.service.ConflictSessionService;
import com.feeltens.git.service.JGitService;
import com.feeltens.git.service.RepoCacheService;
import com.feeltens.git.sse.SseProgressManager;
import com.feeltens.git.vo.req.CommitConflictReqVO;
import com.feeltens.git.vo.req.InitConflictReqVO;
import com.feeltens.git.vo.req.ResolveFileReqVO;
import com.feeltens.git.vo.resp.CommitResultVO;
import com.feeltens.git.vo.resp.ConflictDetailVO;
import com.feeltens.git.vo.resp.ConflictFileVO;
import com.feeltens.git.vo.resp.ConflictSessionVO;
import com.feeltens.git.vo.resp.InitProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * 冲突解决会话服务实现
 *
 * @author feeltens
 */
@Service
@Slf4j
public class ConflictSessionServiceImpl implements ConflictSessionService {

    /**
     * 会话缓存 (projectName -> (sessionId -> ConflictSession))
     */
    private final Map<String, Map<String, ConflictSession>> sessionCache = new ConcurrentHashMap<>();

    /**
     * 进度缓存 (sessionId -> InitProgressVO)
     */
    private final Map<String, InitProgressVO> progressCache = new ConcurrentHashMap<>();

    @Resource(name = "asyncTaskExecutor")
    private Executor asyncTaskExecutor;

    @Resource
    private JGitConfig jgitConfig;
    @Resource
    private GitMergeFlowConfig gitMergeFlowConfig;
    @Resource
    private CodeupConfig codeupConfig;
    @Resource
    private GitLabConfig gitLabConfig;
    @Resource
    private GitProjectMapper gitProjectMapper;
    @Resource
    private JGitService jGitService;
    @Resource
    private ConflictParser conflictParser;
    @Resource
    private GitOpenApiFactory gitOpenApiFactory;
    @Resource
    private SseProgressManager sseProgressManager;
    @Resource
    private RepoCacheService repoCacheService;

    @Override
    public ConflictSessionVO initSession(InitConflictReqVO req) {
        // 1. 参数校验
        validateInitRequest(req);

        // 2. 查询项目信息
        GitProjectDO project = gitProjectMapper.selectByProjectId(req.getProjectId());
        if (project == null) {
            throw new BizException("项目不存在: " + req.getProjectId());
        }

        String projectName = project.getProjectName();

        // 3. 检查该工程的并发会话数
        Map<String, ConflictSession> projectSessions = sessionCache.get(projectName);
        if (projectSessions != null && projectSessions.size() >= jgitConfig.getMaxSessionsPerProject()) {
            throw new BizException("该工程并发会话数已达上限，请稍后重试");
        }

        // 4. 创建会话
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR, jgitConfig.getSessionExpireHours());
        Date expireTime = calendar.getTime();

        ConflictSession session = ConflictSession.builder()
                .sessionId(sessionId)
                .projectId(req.getProjectId())
                .projectName(projectName)
                .repositoryId(project.getRepositoryId())
                .organizationId(project.getOrganizationId())
                .repoUrl(project.getRepositoryUrl())
                .sourceBranch(req.getSourceBranch())
                .targetBranch(req.getTargetBranch())
                .createTime(now)
                .expireTime(expireTime)
                .operator(req.getOperator())
                .status(SessionStatus.INITIALIZING)
                .mixBranchId(req.getMixBranchId())
                .resolvedContents(new ConcurrentHashMap<>())
                .build();

        // 5. 初始化进度信息
        InitProgressVO progress = InitProgressVO.builder()
                .sessionId(sessionId)
                .status(InitProgressStatus.QUEUED)
                .progress(0)
                .currentStep("等待初始化")
                .build();
        progressCache.put(sessionId, progress);

        // 保存初始化请求信息（用于异步初始化）
        initRequestCache.put(sessionId, req);

        // 6. 保存会话（初始状态）
        sessionCache.computeIfAbsent(projectName, k -> new ConcurrentHashMap<>())
                .put(sessionId, session);

        // 7. 推送初始进度（如果SSE连接已存在）
        sseProgressManager.sendProgress(sessionId, progress);

        // 8. 不在这里触发异步初始化，而是在SSE连接建立后触发
        // 这样可以确保SSE连接存在时才开始推送进度

        // 9. 立即返回
        log.info("异步初始化会话已启动: sessionId={}, projectName={}", sessionId, projectName);
        return ConflictSessionVO.builder()
                .sessionId(sessionId)
                .totalFiles(0)
                .resolvedFiles(0)
                .sourceBranch(req.getSourceBranch())
                .targetBranch(req.getTargetBranch())
                .expireTime(expireTime)
                .status(SessionStatus.INITIALIZING.getCode())
                .build();
    }

    @Override
    public ConflictSession getSession(String sessionId) {
        // 遍历所有项目查找 session
        for (Map.Entry<String, Map<String, ConflictSession>> projectEntry : sessionCache.entrySet()) {
            ConflictSession session = projectEntry.getValue().get(sessionId);
            if (session != null) {
                if (session.getExpireTime().before(new Date())) {
                    projectEntry.getValue().remove(sessionId);
                    jGitService.cleanup(session.getProjectName(), sessionId);
                    // 如果项目下没有 session 了，移除项目 entry
                    if (projectEntry.getValue().isEmpty()) {
                        sessionCache.remove(projectEntry.getKey());
                    }
                    throw new BizException("会话已过期: " + sessionId);
                }
                return session;
            }
        }
        throw new BizException("会话不存在或已过期: " + sessionId);
    }

    @Override
    public List<ConflictFileVO> getConflictFiles(String sessionId) {
        ConflictSession session = getSession(sessionId);
        List<ConflictFileVO> result = new ArrayList<>();

        for (String filePath : session.getConflictFiles()) {
            ConflictFileContent content = jGitService.getConflictFileContent(session.getLocalRepoPath(), filePath);
            ParsedConflict parsed = conflictParser.parseConflict(content.getMergedContent(), filePath);

            // 检查是否已解决
            boolean resolved = session.getResolvedContents().containsKey(filePath);
            String currentContent = resolved ? session.getResolvedContents().get(filePath) : content.getMergedContent();
            boolean hasConflicts = conflictParser.hasConflictMarkers(currentContent);

            result.add(ConflictFileVO.builder()
                    .filePath(filePath)
                    .fileName(getFileName(filePath))
                    .conflictCount(parsed.getTotalConflicts())
                    .resolvedCount(resolved && !hasConflicts ? parsed.getTotalConflicts() : 0)
                    .fullyResolved(resolved && !hasConflicts)
                    .fileType(getFileType(filePath))
                    .build());
        }

        return result;
    }

    @Override
    public ConflictDetailVO getConflictDetail(String sessionId, String filePath) {
        ConflictSession session = getSession(sessionId);

        // 延迟加载：检查是否已缓存完整的ConflictFileContent
        ConflictFileContent content = session.getOriginalConflictContents().get(filePath);
        if (content == null) {
            log.info("延迟加载冲突文件内容: sessionId={}, filePath={}", sessionId, filePath);

            content = jGitService.getConflictFileContent(session.getLocalRepoPath(), filePath);

            // 文件大小检查
            String mergedContent = content.getMergedContent();
            long fileSize = mergedContent.length() * 2L;
            if (fileSize > jgitConfig.getMaxConflictFileSize()) {
                throw new BizException(String.format(
                        "文件 %s 过大（%.2fMB），超过限制（%.2fMB）",
                        filePath, fileSize / (1024.0 * 1024),
                        jgitConfig.getMaxConflictFileSize() / (1024.0 * 1024)));
            }

            // 缓存完整的ConflictFileContent对象
            session.getOriginalConflictContents().put(filePath, content);
            log.debug("缓存冲突文件内容: filePath={}, size={}KB", filePath, fileSize / 1024);
        }

        // 获取当前内容（可能是已解决的内容）
        String currentContent = session.getResolvedContents().containsKey(filePath)
                ? session.getResolvedContents().get(filePath)
                : content.getMergedContent();

        boolean resolved = !conflictParser.hasConflictMarkers(currentContent);

        // 解析冲突块
        ParsedConflict parsedConflict = conflictParser.parseConflict(content.getMergedContent(), filePath);

        return ConflictDetailVO.builder()
                .filePath(filePath)
                .oursContent(content.getOursContent())
                .theirsContent(content.getTheirsContent())
                .baseContent(content.getBaseContent())
                .currentContent(currentContent)
                .conflictBlocks(parsedConflict.getConflictBlocks())
                .nonConflictSegments(parsedConflict.getNonConflictSegments())
                .totalConflicts(parsedConflict.getTotalConflicts())
                .fileType(getFileType(filePath))
                .resolved(resolved)
                .sourceBranch(session.getSourceBranch())
                .targetBranch(session.getTargetBranch())
                .build();
    }

    @Override
    public void resolveFile(String sessionId, ResolveFileReqVO req) {
        ConflictSession session = getSession(sessionId);

        if (StrUtil.isBlank(req.getFilePath())) {
            throw new BizException("文件路径不能为空");
        }
        if (!session.getConflictFiles().contains(req.getFilePath())) {
            throw new BizException("文件不在冲突列表中: " + req.getFilePath());
        }

        // 保存解决后的内容到缓存
        session.getResolvedContents().put(req.getFilePath(), req.getResolvedContent());
        session.setStatus(SessionStatus.RESOLVING);

        // 同时写入本地仓库
        jGitService.writeResolvedContent(session.getLocalRepoPath(), req.getFilePath(), req.getResolvedContent());

        log.info("文件冲突已解决: sessionId={}, filePath={}", sessionId, req.getFilePath());
    }

    @Override
    public CommitResultVO commitAndPush(String sessionId, CommitConflictReqVO req) {
        ConflictSession session = getSession(sessionId);

        // 1. 检查是否所有冲突都已解决
        for (String filePath : session.getConflictFiles()) {
            String content = session.getResolvedContents().get(filePath);
            if (content == null || conflictParser.hasConflictMarkers(content)) {
                throw new BizException("存在未解决的冲突文件: " + filePath);
            }
        }

        // 2. 确保所有解决后的内容都已写入
        for (Map.Entry<String, String> entry : session.getResolvedContents().entrySet()) {
            jGitService.writeResolvedContent(session.getLocalRepoPath(), entry.getKey(), entry.getValue());
        }

        // 3. 提交
        String commitMessage = StrUtil.isNotBlank(req.getCommitMessage())
                ? req.getCommitMessage()
                : String.format("Merge branch '%s' into '%s' (conflict resolved)",
                session.getSourceBranch(), session.getTargetBranch());
        String authorName = StrUtil.isNotBlank(req.getAuthorName()) ? req.getAuthorName() : session.getOperator();
        String authorEmail = StrUtil.isNotBlank(req.getAuthorEmail()) ? req.getAuthorEmail() : authorName + "@git-merge-flow";

        jGitService.commitResolved(session.getLocalRepoPath(), commitMessage, authorName, authorEmail);

        // 4. 推送
        GitCredentials credentials = getGitCredentials();
        jGitService.push(session.getLocalRepoPath(), credentials, session.getTargetBranch());

        // 5. 更新会话状态
        session.setStatus(SessionStatus.COMMITTED);

        // 6. 清理资源和缓存
        String projectName = session.getProjectName();
        long freedMemory = calculateSessionMemorySize(session);

        // 清理内存缓存
        if (session.getOriginalConflictContents() != null) {
            session.getOriginalConflictContents().clear();
        }
        if (session.getResolvedContents() != null) {
            session.getResolvedContents().clear();
        }

        jGitService.cleanup(projectName, sessionId);
        Map<String, ConflictSession> projectSessions = sessionCache.get(projectName);
        if (projectSessions != null) {
            projectSessions.remove(sessionId);
            if (projectSessions.isEmpty()) {
                sessionCache.remove(projectName);
            }
        }

        log.info("冲突解决提交成功: sessionId={}, targetBranch={}, 释放内存约={}KB",
                sessionId, session.getTargetBranch(), freedMemory / 1024);

        return CommitResultVO.builder()
                .success(true)
                .message("冲突解决并推送成功")
                .targetBranch(session.getTargetBranch())
                .build();
    }

    @Override
    public void cancelSession(String sessionId) {
        for (Map.Entry<String, Map<String, ConflictSession>> projectEntry : sessionCache.entrySet()) {
            ConflictSession session = projectEntry.getValue().get(sessionId);
            if (session != null) {
                // 计算释放的内存
                long freedMemory = calculateSessionMemorySize(session);

                session.setStatus(SessionStatus.CANCELLED);

                // 清理缓存内容
                if (session.getOriginalConflictContents() != null) {
                    session.getOriginalConflictContents().clear();
                }
                if (session.getResolvedContents() != null) {
                    session.getResolvedContents().clear();
                }

                jGitService.cleanup(session.getProjectName(), sessionId);
                projectEntry.getValue().remove(sessionId);

                if (projectEntry.getValue().isEmpty()) {
                    sessionCache.remove(projectEntry.getKey());
                }

                log.info("会话已取消: sessionId={}, 释放内存约={}KB", sessionId, freedMemory / 1024);
                return;
            }
        }
    }

    @Override
    public void resetFile(String sessionId, String filePath) {
        ConflictSession session = getSession(sessionId);

        if (StrUtil.isBlank(filePath)) {
            throw new BizException("文件路径不能为空");
        }
        if (!session.getConflictFiles().contains(filePath)) {
            throw new BizException("文件不在冲突列表中: " + filePath);
        }

        // 从缓存中移除解决后的内容
        session.getResolvedContents().remove(filePath);

        // 从原始缓存中获取冲突内容
        ConflictFileContent content = session.getOriginalConflictContents().get(filePath);
        if (content == null) {
            log.warn("未找到文件的原始冲突内容缓存，尝试重新读取: filePath={}", filePath);
            // 如果缓存中没有，尝试重新从 Git 获取（兜底方案）
            try {
                content = jGitService.getConflictFileContent(session.getLocalRepoPath(), filePath);
                // 重新缓存
                session.getOriginalConflictContents().put(filePath, content);
            } catch (Exception e) {
                log.error("重新读取原始冲突内容失败: {}", e.getMessage(), e);
                throw new BizException("重置失败，无法获取原始冲突内容: " + e.getMessage());
            }
        }

        // 写入原始冲突内容到工作区
        jGitService.writeResolvedContent(session.getLocalRepoPath(), filePath, content.getMergedContent());

        log.info("文件已重置到原始冲突状态: sessionId={}, filePath={}, operator={}",
                sessionId, filePath, session.getOperator());
    }

    @Override
    @Scheduled(cron = "${merge-flow.schedule.cleanup-expired-sessions}") // 每小时执行一次
    public void cleanupExpiredSessions() {
        Date now = new Date();
        List<String> emptyProjects = new ArrayList<>();
        int cleanedCount = 0;
        long freedMemory = 0;

        for (Map.Entry<String, Map<String, ConflictSession>> projectEntry : sessionCache.entrySet()) {
            String projectName = projectEntry.getKey();
            Map<String, ConflictSession> projectSessions = projectEntry.getValue();
            List<String> expiredSessions = new ArrayList<>();

            for (Map.Entry<String, ConflictSession> sessionEntry : projectSessions.entrySet()) {
                ConflictSession session = sessionEntry.getValue();
                if (session.getExpireTime().before(now)) {
                    // 计算释放的内存大小
                    long sessionMemory = calculateSessionMemorySize(session);
                    freedMemory += sessionMemory;

                    expiredSessions.add(sessionEntry.getKey());
                }
            }

            for (String sessionId : expiredSessions) {
                jGitService.cleanup(projectName, sessionId);
                projectSessions.remove(sessionId);
                cleanedCount++;
                log.info("已清理过期会话: projectName={}, sessionId={}", projectName, sessionId);
            }

            if (projectSessions.isEmpty()) {
                emptyProjects.add(projectName);
            }
        }

        // 移除空的项目 entry
        for (String projectName : emptyProjects) {
            sessionCache.remove(projectName);
        }

        // 同时清理文件系统中的过期仓库
        jGitService.cleanupExpired(jgitConfig.getSessionExpireHours());

        if (cleanedCount > 0) {
            log.info("定期清理完成: 清理会话数={}, 释放内存约={}MB, 当前活跃会话数={}",
                    cleanedCount, freedMemory / (1024 * 1024), getTotalSessionCount());
        }

        // 记录当前缓存使用情况
        logCacheStatistics();
    }

    @Override
    public void clearSessionsByProjectName(String projectName) {
        Map<String, ConflictSession> projectSessions = sessionCache.remove(projectName);
        if (projectSessions != null && !projectSessions.isEmpty()) {
            // 计算释放的内存
            long freedMemory = projectSessions.values().stream()
                    .mapToLong(this::calculateSessionMemorySize)
                    .sum();

            log.info("清理项目会话: projectName={}, sessionCount={}, 释放内存约={}MB",
                    projectName, projectSessions.size(), freedMemory / (1024 * 1024));
        }
        // 清理该项目的所有临时仓库目录
        jGitService.cleanupByProjectName(projectName);
    }

    /**
     * 定期记录缓存统计信息（每30分钟）
     */
    @Scheduled(cron = "${merge-flow.schedule.report-cache-statistics}")
    public void reportCacheStatistics() {
        int totalSessions = getTotalSessionCount();
        if (totalSessions > 0) {
            logCacheStatistics();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 通过API获取变更文件列表（用于稀疏检出优化）
     */
    private List<String> getChangedFilesFromAPI(GitProjectDO project, String sourceBranch, String targetBranch) {
        GetCompareReq compareReq = new GetCompareReq();

        // 设置基础配置
        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.CODEUP.getCode())) {
            compareReq.setBaseUrl(codeupConfig.getBaseUrl());
            compareReq.setAccessToken(codeupConfig.getAccessToken());
            compareReq.setOrganizationId(project.getOrganizationId());
        } else if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.GITLAB.getCode())) {
            compareReq.setBaseUrl(gitLabConfig.getBaseUrl());
            compareReq.setAccessToken(gitLabConfig.getAccessToken());
        } else {
            throw new BizException("未配置的 Git 服务平台");
        }

        compareReq.setRepositoryId(project.getRepositoryId());
        compareReq.setFrom(targetBranch);  // 目标分支作为基准
        compareReq.setTo(sourceBranch);    // 源分支作为比较对象

        // 调用API
        GetCompareResp compareResp = gitOpenApiFactory.getCompare(compareReq);

        // 提取变更文件路径
        Set<String> files = new HashSet<>();
        if (compareResp.getDiffs() != null && !compareResp.getDiffs().isEmpty()) {
            for (GetCompareResp.DiffFile diff : compareResp.getDiffs()) {
                if (diff.getOldPath() != null && !diff.getOldPath().isEmpty()) {
                    files.add(diff.getOldPath());
                }
                if (diff.getNewPath() != null && !diff.getNewPath().isEmpty()) {
                    files.add(diff.getNewPath());
                }
            }
        }

        return new ArrayList<>(files);
    }

    private void validateInitRequest(InitConflictReqVO req) {
        if (req == null) {
            throw new BizException("请求参数不能为空");
        }
        if (req.getProjectId() == null) {
            throw new BizException("项目ID不能为空");
        }
        if (StrUtil.isBlank(req.getSourceBranch())) {
            throw new BizException("源分支不能为空");
        }
        if (StrUtil.isBlank(req.getTargetBranch())) {
            throw new BizException("目标分支不能为空");
        }
        if (StrUtil.isBlank(req.getOperator())) {
            throw new BizException("操作人不能为空");
        }
    }

    private GitCredentials getGitCredentials() {
        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.CODEUP.getCode())) {
            return GitCredentials.builder()
                    .username("oauth2")
                    .password(codeupConfig.getAccessToken())
                    .build();
        }
        if (StrUtil.equals(gitMergeFlowConfig.getGitService(), GitServiceEnum.GITLAB.getCode())) {
            return GitCredentials.builder()
                    .username("oauth2")
                    .password(gitLabConfig.getAccessToken())
                    .build();
        }
        throw new BizException("未配置的 Git 服务平台");
    }

    private String getFileName(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return "";
        }
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String getFileType(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return "text";
        }
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot < 0) {
            return "text";
        }
        String ext = filePath.substring(lastDot + 1).toLowerCase();
        switch (ext) {
            case "java":
                return "java";
            case "js":
                return "javascript";
            case "ts":
                return "typescript";
            case "xml":
                return "xml";
            case "json":
                return "json";
            case "yml":
            case "yaml":
                return "yaml";
            case "html":
                return "html";
            case "css":
                return "css";
            case "sql":
                return "sql";
            case "py":
                return "python";
            case "md":
                return "markdown";
            default:
                return "text";
        }
    }

    /**
     * 计算会话占用的内存大小（字节）
     */
    private long calculateSessionMemorySize(ConflictSession session) {
        long size = 0;

        // 计算原始冲突内容缓存大小（ConflictFileContent对象）
        if (session.getOriginalConflictContents() != null) {
            for (ConflictFileContent content : session.getOriginalConflictContents().values()) {
                if (content != null) {
                    // 计算各个字段的大小
                    if (content.getOursContent() != null) {
                        size += content.getOursContent().length() * 2;
                    }
                    if (content.getTheirsContent() != null) {
                        size += content.getTheirsContent().length() * 2;
                    }
                    if (content.getBaseContent() != null) {
                        size += content.getBaseContent().length() * 2;
                    }
                    if (content.getMergedContent() != null) {
                        size += content.getMergedContent().length() * 2;
                    }
                }
            }
        }

        // 计算已解决内容缓存大小
        if (session.getResolvedContents() != null) {
            for (String content : session.getResolvedContents().values()) {
                if (content != null) {
                    size += content.length() * 2;
                }
            }
        }

        return size;
    }

    /**
     * 获取当前活跃会话总数
     */
    private int getTotalSessionCount() {
        return sessionCache.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    /**
     * 获取缓存总大小（字节）
     */
    private long getTotalCacheSize() {
        return sessionCache.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToLong(this::calculateSessionMemorySize)
                .sum();
    }

    /**
     * 记录缓存统计信息
     */
    private void logCacheStatistics() {
        int totalSessions = getTotalSessionCount();
        long totalSize = getTotalCacheSize();
        int projectCount = sessionCache.size();

        if (totalSessions > 0) {
            log.info("缓存统计: 项目数={}, 会话数={}, 总内存占用={}MB, 平均每会话={}KB",
                    projectCount, totalSessions,
                    totalSize / (1024 * 1024),
                    totalSize / totalSessions / 1024);
        }

        // 如果缓存过大，发出警告
        long maxCacheSize = 500L * 1024 * 1024; // 500MB
        if (totalSize > maxCacheSize) {
            log.warn("缓存占用过大: {}MB，建议检查会话清理策略", totalSize / (1024 * 1024));
        }
    }

    /**
     * 存储初始化请求信息（用于异步初始化）
     */
    private final Map<String, InitConflictReqVO> initRequestCache = new ConcurrentHashMap<>();

    /**
     * 异步初始化会话
     */
    private void asyncInitializeSession(ConflictSession session, GitProjectDO project,
                                        InitConflictReqVO req, InitProgressVO progress) {
        String sessionId = session.getSessionId();
        String projectName = session.getProjectName();
        Long projectId = project.getProjectId();

        try {
            // 步骤1: 确保缓存就绪 (10% -> 30%)
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 10, "检查缓存状态");
            repoCacheService.ensureCacheReady(projectId);
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 30, "缓存就绪");

            // 步骤2: 从缓存拷贝到会话目录 (30% -> 60%)
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 40, "拷贝仓库中");
            String localRepoPath = repoCacheService.copyToSessionDir(projectId, sessionId);
            session.setLocalRepoPath(localRepoPath);
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 60, "仓库拷贝完成");

            // 步骤2.5: Fetch 所有远程分支 (60% -> 65%)
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 60, "同步远程分支");
            GitCredentials credentials = getGitCredentials();
            jGitService.fetch(localRepoPath, credentials);
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 65, "远程分支同步完成");

            // 步骤2.6: 重置本地分支到远程分支 (65% -> 70%)
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 65, "更新本地分支");
            jGitService.resetToRemote(localRepoPath, req.getSourceBranch());
            jGitService.resetToRemote(localRepoPath, req.getTargetBranch());
            updateProgress(sessionId, progress, InitProgressStatus.CLONING, 70, "本地分支更新完成");

            // 步骤3: 执行合并 (70% -> 80%)
            updateProgress(sessionId, progress, InitProgressStatus.MERGING, 70, "合并分支中");
            MergeResult mergeResult = jGitService.merge(localRepoPath, req.getSourceBranch(), req.getTargetBranch());

            if (!mergeResult.getHasConflicts()) {
                jGitService.cleanup(projectName, sessionId);
                updateProgress(sessionId, progress, InitProgressStatus.FAILED, -1, "分支合并无冲突，无需手动解决");
                session.setStatus(SessionStatus.FAILED);
                session.setErrorMessage("分支合并无冲突，无需手动解决");
                return;
            }

            // 步骤4: 获取冲突文件列表 (70% -> 90%)
            updateProgress(sessionId, progress, InitProgressStatus.LOADING_CONFLICTS, 80, "加载冲突文件列表");
            List<String> conflictFiles = jGitService.getConflictFiles(localRepoPath);
            session.setConflictFiles(conflictFiles);

            // 延迟加载：只缓存文件列表，不缓存内容
            session.setOriginalConflictContents(new ConcurrentHashMap<>());

            progress.setTotalFiles(conflictFiles.size());
            updateProgress(sessionId, progress, InitProgressStatus.LOADING_CONFLICTS, 90, "加载完成");

            // 完成 (100%)
            updateProgress(sessionId, progress, InitProgressStatus.READY, 100, "初始化完成");
            session.setStatus(SessionStatus.READY);

            log.info("异步初始化成功: sessionId={}, conflictFiles={}",
                    sessionId, conflictFiles.size());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void triggerAsyncInit(String sessionId) {
        ConflictSession session = getSession(sessionId);
        if (session == null) {
            log.warn("会话不存在，无法触发异步初始化: sessionId={}", sessionId);
            return;
        }

        // 检查会话状态，避免重复初始化
        if (session.getStatus() != SessionStatus.INITIALIZING) {
            log.warn("会话状态不是INITIALIZING，无需触发异步初始化: sessionId={}, status={}",
                    sessionId, session.getStatus());
            return;
        }

        // 获取存储的初始化请求
        InitConflictReqVO req = initRequestCache.get(sessionId);
        if (req == null) {
            log.error("初始化请求信息不存在: sessionId={}", sessionId);
            return;
        }

        // 获取项目信息
        GitProjectDO project = gitProjectMapper.selectByProjectId(req.getProjectId());
        if (project == null) {
            log.error("项目不存在: projectId={}", req.getProjectId());
            return;
        }

        // 获取进度信息
        InitProgressVO progress = progressCache.get(sessionId);
        if (progress == null) {
            log.error("进度信息不存在: sessionId={}", sessionId);
            return;
        }

        // 触发异步初始化
        asyncTaskExecutor.execute(() -> {
            try {
                asyncInitializeSession(session, project, req, progress);
            } catch (Exception e) {
                handleInitFailure(session, progress, e);
            }
        });

        log.info("已触发异步初始化: sessionId={}", sessionId);
    }

    /**
     * 处理初始化失败
     */
    private void handleInitFailure(ConflictSession session, InitProgressVO progress, Exception e) {
        String sessionId = session.getSessionId();
        String projectName = session.getProjectName();

        updateProgress(sessionId, progress, InitProgressStatus.FAILED, -1, "初始化失败: " + e.getMessage());
        session.setStatus(SessionStatus.FAILED);
        session.setErrorMessage(e.getMessage());

        // 清理资源
        jGitService.cleanup(projectName, sessionId);

        // 清理缓存
        progressCache.remove(sessionId);
        initRequestCache.remove(sessionId); // 清理初始化请求缓存
        Map<String, ConflictSession> projectSessions = sessionCache.get(projectName);
        if (projectSessions != null) {
            projectSessions.remove(sessionId);
            if (projectSessions.isEmpty()) {
                sessionCache.remove(projectName);
            }
        }

        log.error("异步初始化失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
    }

    /**
     * 更新进度信息
     */
    private void updateProgress(String sessionId, InitProgressVO progress,
                                InitProgressStatus status, int progressValue, String step) {
        progress.setStatus(status);
        progress.setProgress(progressValue);
        progress.setCurrentStep(step);
        progressCache.put(sessionId, progress);
        sseProgressManager.sendProgress(sessionId, progress);
    }

    /**
     * 获取初始化进度
     */
    @Override
    public InitProgressVO getInitProgress(String sessionId) {
        return progressCache.get(sessionId);
    }

}