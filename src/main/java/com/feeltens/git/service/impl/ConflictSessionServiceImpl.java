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
import com.feeltens.git.enums.SessionStatus;
import com.feeltens.git.mapper.GitProjectMapper;
import com.feeltens.git.service.ConflictParser;
import com.feeltens.git.service.ConflictSessionService;
import com.feeltens.git.service.JGitService;
import com.feeltens.git.vo.req.CommitConflictReqVO;
import com.feeltens.git.vo.req.InitConflictReqVO;
import com.feeltens.git.vo.req.ResolveFileReqVO;
import com.feeltens.git.vo.resp.CommitResultVO;
import com.feeltens.git.vo.resp.ConflictDetailVO;
import com.feeltens.git.vo.resp.ConflictFileVO;
import com.feeltens.git.vo.resp.ConflictSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

        try {
            // 5. 克隆仓库
            GitCredentials credentials = getGitCredentials();
            String localRepoPath = jGitService.cloneRepository(project.getRepositoryUrl(), projectName, sessionId, credentials);
            session.setLocalRepoPath(localRepoPath);

            // 6. 执行合并
            MergeResult mergeResult = jGitService.merge(localRepoPath, req.getSourceBranch(), req.getTargetBranch());

            if (!mergeResult.getHasConflicts()) {
                // 无冲突，清理并返回
                jGitService.cleanup(projectName, sessionId);
                throw new BizException("分支合并无冲突，无需手动解决");
            }

            // 7. 获取冲突文件列表
            List<String> conflictFiles = jGitService.getConflictFiles(localRepoPath);
            session.setConflictFiles(conflictFiles);
            session.setStatus(SessionStatus.READY);

            // 8. 保存会话
            sessionCache.computeIfAbsent(projectName, k -> new ConcurrentHashMap<>())
                    .put(sessionId, session);

            log.info("冲突解决会话初始化成功: projectName={}, sessionId={}, conflictFiles={}",
                    projectName, sessionId, conflictFiles.size());

            return ConflictSessionVO.builder()
                    .sessionId(sessionId)
                    .totalFiles(conflictFiles.size())
                    .resolvedFiles(0)
                    .sourceBranch(req.getSourceBranch())
                    .targetBranch(req.getTargetBranch())
                    .expireTime(expireTime)
                    .status(SessionStatus.READY.getCode())
                    .build();

        } catch (Exception e) {
            // 初始化失败，清理资源
            jGitService.cleanup(projectName, sessionId);
            log.error("冲突解决会话初始化失败: {}", e.getMessage(), e);
            throw new BizException("初始化失败: " + e.getMessage());
        }
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

        ConflictFileContent content = jGitService.getConflictFileContent(session.getLocalRepoPath(), filePath);

        // 获取当前内容（可能是已解决的内容）
        String currentContent = session.getResolvedContents().containsKey(filePath)
                ? session.getResolvedContents().get(filePath)
                : content.getMergedContent();

        boolean resolved = !conflictParser.hasConflictMarkers(currentContent);

        return ConflictDetailVO.builder()
                .filePath(filePath)
                .oursContent(content.getOursContent())
                .theirsContent(content.getTheirsContent())
                .baseContent(content.getBaseContent())
                .currentContent(currentContent)
                .conflictBlocks(content.getConflictBlocks())
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

        // 6. 清理资源
        String projectName = session.getProjectName();
        jGitService.cleanup(projectName, sessionId);
        Map<String, ConflictSession> projectSessions = sessionCache.get(projectName);
        if (projectSessions != null) {
            projectSessions.remove(sessionId);
            if (projectSessions.isEmpty()) {
                sessionCache.remove(projectName);
            }
        }

        log.info("冲突解决提交成功: sessionId={}, targetBranch={}", sessionId, session.getTargetBranch());

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
                session.setStatus(SessionStatus.CANCELLED);
                jGitService.cleanup(session.getProjectName(), sessionId);
                projectEntry.getValue().remove(sessionId);
                if (projectEntry.getValue().isEmpty()) {
                    sessionCache.remove(projectEntry.getKey());
                }
                log.info("会话已取消: {}", sessionId);
                return;
            }
        }
    }

    @Override
    public void resetFile(String sessionId, String filePath) {
        ConflictSession session = getSession(sessionId);

        // 从缓存中移除解决后的内容
        session.getResolvedContents().remove(filePath);

        // 重新获取原始冲突内容并写入
        ConflictFileContent content = jGitService.getConflictFileContent(session.getLocalRepoPath(), filePath);
        jGitService.writeResolvedContent(session.getLocalRepoPath(), filePath, content.getMergedContent());

        log.info("文件已重置: sessionId={}, filePath={}", sessionId, filePath);
    }

    @Override
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void cleanupExpiredSessions() {
        Date now = new Date();
        List<String> emptyProjects = new ArrayList<>();

        for (Map.Entry<String, Map<String, ConflictSession>> projectEntry : sessionCache.entrySet()) {
            String projectName = projectEntry.getKey();
            Map<String, ConflictSession> projectSessions = projectEntry.getValue();
            List<String> expiredSessions = new ArrayList<>();

            for (Map.Entry<String, ConflictSession> sessionEntry : projectSessions.entrySet()) {
                if (sessionEntry.getValue().getExpireTime().before(now)) {
                    expiredSessions.add(sessionEntry.getKey());
                }
            }

            for (String sessionId : expiredSessions) {
                jGitService.cleanup(projectName, sessionId);
                projectSessions.remove(sessionId);
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
    }

    @Override
    public void clearSessionsByProjectName(String projectName) {
        Map<String, ConflictSession> projectSessions = sessionCache.remove(projectName);
        if (projectSessions != null && !projectSessions.isEmpty()) {
            log.info("清理项目会话: projectName={}, sessionCount={}", projectName, projectSessions.size());
        }
        // 清理该项目的所有临时仓库目录
        jGitService.cleanupByProjectName(projectName);
    }

    // ==================== 私有方法 ====================

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

}