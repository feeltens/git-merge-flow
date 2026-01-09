package com.feeltens.git.service.impl;

import cn.hutool.core.io.FileUtil;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.config.JGitConfig;
import com.feeltens.git.dto.conflict.ConflictBlock;
import com.feeltens.git.dto.conflict.ConflictFileContent;
import com.feeltens.git.dto.conflict.GitCredentials;
import com.feeltens.git.dto.conflict.MergeResult;
import com.feeltens.git.dto.conflict.ParsedConflict;
import com.feeltens.git.service.ConflictParser;
import com.feeltens.git.service.JGitService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JGit 服务实现
 *
 * @author feeltens
 */
@Service
@Slf4j
public class JGitServiceImpl implements JGitService {

    @Resource
    private JGitConfig jgitConfig;
    @Resource
    private ConflictParser conflictParser;

    @Override
    public String cloneRepository(String repoUrl, String projectName, String sessionId, GitCredentials credentials) {
        String localPath = getLocalRepoPath(projectName, sessionId);
        File localDir = new File(localPath);

        // 如果目录已存在，先清理
        if (localDir.exists()) {
            FileUtil.del(localDir);
        }

        log.info("开始克隆仓库: {} -> {}", repoUrl, localPath);

        try {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    credentials.getUsername(), credentials.getPassword());

            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localDir)
                    .setCredentialsProvider(credentialsProvider)
                    .setCloneAllBranches(true)
                    .setTimeout(jgitConfig.getCloneTimeoutSeconds())
                    .call()
                    .close();

            log.info("仓库克隆成功: {}", localPath);
            return localPath;

        } catch (GitAPIException e) {
            log.error("克隆仓库失败: {}", e.getMessage(), e);
            // 清理失败的目录
            if (localDir.exists()) {
                FileUtil.del(localDir);
            }
            throw new BizException("克隆仓库失败: " + e.getMessage());
        }
    }

    @Override
    public MergeResult merge(String localRepoPath, String sourceBranch, String targetBranch) {
        log.info("开始合并: {} -> {}, 仓库: {}", sourceBranch, targetBranch, localRepoPath);

        try (Git git = Git.open(new File(localRepoPath))) {
            // 1. 切换到目标分支（从远程分支创建本地分支）
            // 先检查本地分支是否存在
            boolean localBranchExists = git.branchList().call().stream()
                    .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

            if (localBranchExists) {
                // 本地分支存在，直接切换
                git.checkout()
                        .setName(targetBranch)
                        .call();
            } else {
                // 本地分支不存在，从远程分支创建并切换
                git.checkout()
                        .setName(targetBranch)
                        .setCreateBranch(true)
                        .setStartPoint("origin/" + targetBranch)
                        .setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .call();
            }

            // 2. 获取源分支的引用
            ObjectId sourceCommit = git.getRepository().resolve("origin/" + sourceBranch);
            if (sourceCommit == null) {
                throw new BizException("源分支不存在: " + sourceBranch);
            }

            // 3. 执行合并（不自动提交）
            org.eclipse.jgit.api.MergeResult mergeResult = git.merge()
                    .include(sourceCommit)
                    .setCommit(false)  // 不自动提交，让用户解决冲突后手动提交
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .setStrategy(MergeStrategy.RECURSIVE)
                    .call();

            // 4. 处理合并结果
            org.eclipse.jgit.api.MergeResult.MergeStatus status = mergeResult.getMergeStatus();
            log.info("合并状态: {}", status);

            if (status == org.eclipse.jgit.api.MergeResult.MergeStatus.CONFLICTING) {
                // 有冲突
                Map<String, int[][]> conflicts = mergeResult.getConflicts();
                List<String> conflictFiles = new ArrayList<>();

                if (conflicts != null) {
                    conflictFiles.addAll(conflicts.keySet());
                }

                return MergeResult.builder()
                        .success(false)
                        .hasConflicts(true)
                        .conflictFiles(conflictFiles)
                        .statusMessage("合并存在冲突，需要手动解决")
                        .build();
            } else if (status.isSuccessful()) {
                return MergeResult.builder()
                        .success(true)
                        .hasConflicts(false)
                        .conflictFiles(new ArrayList<>())
                        .statusMessage("合并成功")
                        .build();
            } else {
                return MergeResult.builder()
                        .success(false)
                        .hasConflicts(false)
                        .conflictFiles(new ArrayList<>())
                        .statusMessage("合并失败: " + status.toString())
                        .errorMessage(status.toString())
                        .build();
            }
        } catch (IOException | GitAPIException e) {
            log.error("合并操作失败: {}", e.getMessage(), e);
            throw new BizException("合并操作失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getConflictFiles(String localRepoPath) {
        try (Git git = Git.open(new File(localRepoPath))) {
            // 获取状态中的冲突文件
            return new ArrayList<>(git.status().call().getConflicting());
        } catch (IOException | GitAPIException e) {
            log.error("获取冲突文件列表失败: {}", e.getMessage(), e);
            throw new BizException("获取冲突文件列表失败: " + e.getMessage());
        }
    }

    @Override
    public ConflictFileContent getConflictFileContent(String localRepoPath, String filePath) {
        try (Git git = Git.open(new File(localRepoPath))) {
            Repository repository = git.getRepository();

            // 读取当前工作区的合并内容（包含冲突标记）
            Path workingFile = Paths.get(localRepoPath, filePath);
            String mergedContent = "";
            if (Files.exists(workingFile)) {
                mergedContent = new String(Files.readAllBytes(workingFile), StandardCharsets.UTF_8);
            }

            // 读取 Ours 版本 (HEAD)
            String oursContent = readFileFromRef(repository, "HEAD", filePath);

            // 读取 Theirs 版本 (MERGE_HEAD)
            String theirsContent = readFileFromRef(repository, "MERGE_HEAD", filePath);

            // 读取 Base 版本 (MERGE_BASE)
            String baseContent = readMergeBase(repository, filePath);

            // 解析冲突块
            ParsedConflict parsedConflict = conflictParser.parseConflict(mergedContent, filePath);
            List<ConflictBlock> conflictBlocks = parsedConflict.getConflictBlocks();

            return ConflictFileContent.builder()
                    .filePath(filePath)
                    .baseContent(baseContent)
                    .oursContent(oursContent)
                    .theirsContent(theirsContent)
                    .mergedContent(mergedContent)
                    .conflictBlocks(conflictBlocks)
                    .build();
        } catch (IOException e) {
            log.error("读取冲突文件内容失败: {}", e.getMessage(), e);
            throw new BizException("读取冲突文件内容失败: " + e.getMessage());
        }
    }

    @Override
    public void writeResolvedContent(String localRepoPath, String filePath, String resolvedContent) {
        try {
            Path targetFile = Paths.get(localRepoPath, filePath);
            Files.write(targetFile, resolvedContent.getBytes(StandardCharsets.UTF_8));
            log.info("已写入解决后的文件: {}", filePath);
        } catch (IOException e) {
            log.error("写入解决后的文件失败: {}", e.getMessage(), e);
            throw new BizException("写入解决后的文件失败: " + e.getMessage());
        }
    }

    @Override
    public void commitResolved(String localRepoPath, String commitMessage, String authorName, String authorEmail) {
        try (Git git = Git.open(new File(localRepoPath))) {
            // 1. 添加所有更改到暂存区
            git.add().addFilepattern(".").call();

            // 2. 提交
            PersonIdent author = new PersonIdent(authorName, authorEmail);
            git.commit()
                    .setMessage(commitMessage)
                    .setAuthor(author)
                    .setCommitter(author)
                    .call();

            log.info("冲突解决提交成功: {}", commitMessage);
        } catch (IOException | GitAPIException e) {
            log.error("提交失败: {}", e.getMessage(), e);
            throw new BizException("提交失败: " + e.getMessage());
        }
    }

    @Override
    public void push(String localRepoPath, GitCredentials credentials, String targetBranch) {
        try (Git git = Git.open(new File(localRepoPath))) {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    credentials.getUsername(), credentials.getPassword());

            RefSpec refSpec = new RefSpec(targetBranch + ":" + targetBranch);

            Iterable<PushResult> results = git.push()
                    .setCredentialsProvider(credentialsProvider)
                    .setRefSpecs(refSpec)
                    .setRemote("origin")
                    .call();

            for (PushResult result : results) {
                log.info("推送结果: {}", result.getMessages());
            }

            log.info("推送成功: {}", targetBranch);
        } catch (IOException | GitAPIException e) {
            log.error("推送失败: {}", e.getMessage(), e);
            throw new BizException("推送失败: " + e.getMessage());
        }
    }

    @Override
    public void cleanup(String projectName, String sessionId) {
        String localPath = getLocalRepoPath(projectName, sessionId);
        File localDir = new File(localPath);

        if (localDir.exists()) {
            FileUtil.del(localDir);
            log.info("已清理临时仓库: {}", localPath);
        }

        // 如果项目目录为空，也删除项目目录
        File projectDir = new File(getProjectPath(projectName));
        if (projectDir.exists() && projectDir.isDirectory()) {
            String[] files = projectDir.list();
            if (files == null || files.length == 0) {
                FileUtil.del(projectDir);
                log.info("已清理空的项目目录: {}", projectDir.getAbsolutePath());
            }
        }
    }

    @Override
    public void cleanupByProjectName(String projectName) {
        String projectPath = getProjectPath(projectName);
        File projectDir = new File(projectPath);

        if (projectDir.exists()) {
            FileUtil.del(projectDir);
            log.info("已清理项目临时仓库目录: {}", projectPath);
        }
    }

    @Override
    public void cleanupExpired(int expireHours) {
        File tempDir = new File(jgitConfig.getTempRepoPath());
        if (!tempDir.exists() || !tempDir.isDirectory()) {
            return;
        }

        long expireMillis = expireHours * 60 * 60 * 1000L;
        long now = System.currentTimeMillis();

        // 遍历项目目录
        File[] projectDirs = tempDir.listFiles();
        if (projectDirs == null) {
            return;
        }

        for (File projectDir : projectDirs) {
            if (!projectDir.isDirectory()) {
                continue;
            }

            // 遍历项目下的 session 目录
            File[] sessionDirs = projectDir.listFiles();
            if (sessionDirs == null) {
                continue;
            }

            for (File sessionDir : sessionDirs) {
                if (sessionDir.isDirectory()) {
                    long lastModified = sessionDir.lastModified();
                    if (now - lastModified > expireMillis) {
                        FileUtil.del(sessionDir);
                        log.info("已清理过期临时仓库: {}", sessionDir.getAbsolutePath());
                    }
                }
            }

            // 如果项目目录为空，也删除
            String[] remainingFiles = projectDir.list();
            if (remainingFiles == null || remainingFiles.length == 0) {
                FileUtil.del(projectDir);
                log.info("已清理空的项目目录: {}", projectDir.getAbsolutePath());
            }
        }
    }

    @Override
    public boolean exists(String projectName, String sessionId) {
        String localPath = getLocalRepoPath(projectName, sessionId);
        File localDir = new File(localPath);
        return localDir.exists() && localDir.isDirectory();
    }

    // ==================== 私有方法 ====================

    private String getLocalRepoPath(String projectName, String sessionId) {
        return jgitConfig.getTempRepoPath() + File.separator + projectName + File.separator + sessionId;
    }

    private String getProjectPath(String projectName) {
        return jgitConfig.getTempRepoPath() + File.separator + projectName;
    }

    /**
     * 从指定引用读取文件内容
     */
    private String readFileFromRef(Repository repository, String refName, String filePath) {
        try {
            ObjectId refId = repository.resolve(refName);
            if (refId == null) {
                return "";
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(refId);
                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, commit.getTree())) {
                    if (treeWalk == null) {
                        return "";
                    }
                    ObjectId blobId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(blobId);
                    return new String(loader.getBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            log.warn("读取文件失败 ref={}, file={}: {}", refName, filePath, e.getMessage());
            return "";
        }
    }

    /**
     * 读取合并基础版本
     */
    private String readMergeBase(Repository repository, String filePath) {
        try {
            ObjectId headId = repository.resolve("HEAD");
            ObjectId mergeHeadId = repository.resolve("MERGE_HEAD");

            if (headId == null || mergeHeadId == null) {
                return "";
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit headCommit = revWalk.parseCommit(headId);
                RevCommit mergeHeadCommit = revWalk.parseCommit(mergeHeadId);

                revWalk.setRevFilter(org.eclipse.jgit.revwalk.filter.RevFilter.MERGE_BASE);
                revWalk.markStart(headCommit);
                revWalk.markStart(mergeHeadCommit);

                RevCommit mergeBase = revWalk.next();
                if (mergeBase == null) {
                    return "";
                }

                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, mergeBase.getTree())) {
                    if (treeWalk == null) {
                        return "";
                    }
                    ObjectId blobId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repository.open(blobId);
                    return new String(loader.getBytes(), StandardCharsets.UTF_8);
                }
            }
        } catch (IOException e) {
            log.warn("读取合并基础版本失败 file={}: {}", filePath, e.getMessage());
            return "";
        }
    }

}