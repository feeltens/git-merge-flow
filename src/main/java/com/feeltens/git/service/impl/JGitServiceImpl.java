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
import com.feeltens.git.util.GitPathBuilder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.FileMode;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    @Resource
    private GitPathBuilder gitPathBuilder;

    @Override
    public MergeResult merge(String localRepoPath, String sourceBranch, String targetBranch) {
        log.info("开始合并: {} -> {}, 仓库: {}", sourceBranch, targetBranch, localRepoPath);

        try (Git git = Git.open(new File(localRepoPath))) {
            Repository repository = git.getRepository();

            // 1. 确保在目标分支上（稀疏检出时已经设置了HEAD）
            String currentBranch = repository.getBranch();
            if (!targetBranch.equals(currentBranch)) {
                log.warn("当前分支 {} 与目标分支 {} 不一致，切换分支", currentBranch, targetBranch);

                boolean localBranchExists = git.branchList().call().stream()
                        .anyMatch(ref -> ref.getName().equals("refs/heads/" + targetBranch));

                if (localBranchExists) {
                    git.checkout().setName(targetBranch).call();
                } else {
                    git.checkout()
                            .setName(targetBranch)
                            .setCreateBranch(true)
                            .setStartPoint("origin/" + targetBranch)
                            .setUpstreamMode(org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK)
                            .call();
                }
            }

            // 2. 获取源分支的引用
            ObjectId sourceCommit = repository.resolve("origin/" + sourceBranch);
            if (sourceCommit == null) {
                // 尝试本地分支
                sourceCommit = repository.resolve(sourceBranch);
                if (sourceCommit == null) {
                    throw new BizException("源分支不存在: " + sourceBranch);
                }
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

                // 【新增】检查冲突文件是否都在工作目录中（稀疏检出场景）
                List<String> missingConflictFiles = new ArrayList<>();
                for (String conflictFile : conflictFiles) {
                    File file = new File(localRepoPath, conflictFile);
                    if (!file.exists()) {
                        missingConflictFiles.add(conflictFile);
                    }
                }

                if (!missingConflictFiles.isEmpty()) {
                    log.warn("发现不在稀疏检出中的冲突文件: {}", missingConflictFiles);

                    // 检出缺失的冲突文件
                    try {
                        checkoutMissingConflictFiles(repository, localRepoPath, missingConflictFiles);
                        log.info("已检出缺失的冲突文件: {}", missingConflictFiles.size());
                    } catch (Exception e) {
                        log.error("检出缺失的冲突文件失败: {}", e.getMessage(), e);
                        // 继续处理，让用户知道有冲突
                    }
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

    /**
     * 检出缺失的冲突文件（稀疏检出场景）
     */
    private void checkoutMissingConflictFiles(Repository repository, String localRepoPath,
                                              List<String> missingFiles) throws IOException {
        ObjectId headId = repository.resolve(org.eclipse.jgit.lib.Constants.HEAD);
        if (headId == null) {
            throw new IOException("HEAD未设置");
        }

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(headId);

            for (String filePath : missingFiles) {
                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, commit.getTree())) {
                    if (treeWalk != null) {
                        FileMode fileMode = treeWalk.getFileMode(0);
                        ObjectId blobId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(blobId);

                        File targetFile = new File(localRepoPath, filePath);
                        targetFile.getParentFile().mkdirs();

                        // 根据文件类型检出
                        if (fileMode.equals(FileMode.SYMLINK)) {
                            checkoutSymlink(loader, targetFile);
                        } else {
                            checkoutRegularFile(loader, targetFile, fileMode.equals(FileMode.EXECUTABLE_FILE));
                        }

                        log.info("检出缺失的冲突文件: {}", filePath);
                    }
                } catch (Exception e) {
                    log.error("检出文件失败: {}, error: {}", filePath, e.getMessage());
                }
            }
        }
    }

    /**
     * 清理非稀疏文件（merge后可能产生的额外文件）
     */
    private void cleanupNonSparseFiles(String localRepoPath) throws IOException {
        File sparseCheckoutFile = new File(localRepoPath, ".git/info/sparse-checkout");
        if (!sparseCheckoutFile.exists()) {
            log.debug("sparse-checkout文件不存在，跳过清理");
            return;
        }

        // 读取稀疏检出列表
        List<String> sparsePatterns = Files.readAllLines(
                sparseCheckoutFile.toPath(), StandardCharsets.UTF_8);
        Set<String> sparseSet = new HashSet<>(sparsePatterns);

        // 遍历工作目录，删除不匹配的文件
        File workDir = new File(localRepoPath);
        int cleanedCount = cleanupDirectory(workDir, workDir, sparseSet);

        if (cleanedCount > 0) {
            log.info("清理非稀疏文件完成，删除文件数: {}", cleanedCount);
        }
    }

    /**
     * 递归清理目录中的非稀疏文件
     */
    private int cleanupDirectory(File baseDir, File currentDir, Set<String> sparseSet) {
        int cleanedCount = 0;
        File[] files = currentDir.listFiles();
        if (files == null) {
            return 0;
        }

        for (File file : files) {
            if (file.getName().equals(".git")) {
                continue;
            }

            String relativePath = baseDir.toPath().relativize(file.toPath()).toString();
            relativePath = relativePath.replace("\\", "/");  // Windows兼容

            if (file.isDirectory()) {
                // 递归处理子目录
                cleanedCount += cleanupDirectory(baseDir, file, sparseSet);

                // 如果目录为空，删除
                String[] remaining = file.list();
                if (remaining != null && remaining.length == 0) {
                    if (file.delete()) {
                        log.debug("删除空目录: {}", relativePath);
                    }
                }
            } else {
                // 检查文件是否在稀疏列表中
                if (!matchesSparseCheckout(relativePath, sparseSet)) {
                    if (file.delete()) {
                        log.debug("删除非稀疏文件: {}", relativePath);
                        cleanedCount++;
                    }
                }
            }
        }

        return cleanedCount;
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
    public void fetch(String localRepoPath, GitCredentials credentials) {
        try (Git git = Git.open(new File(localRepoPath))) {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    credentials.getUsername(), credentials.getPassword());

            git.fetch()
                    .setCredentialsProvider(credentialsProvider)
                    .setRemote("origin")
                    .call();

            log.info("Fetch 所有远程分支成功: {}", localRepoPath);
        } catch (IOException | GitAPIException e) {
            log.error("Fetch 失败: {}", e.getMessage(), e);
            throw new BizException("Fetch 失败: " + e.getMessage());
        }
    }

    @Override
    public void resetToRemote(String localRepoPath, String branchName) {
        try (Git git = Git.open(new File(localRepoPath))) {
            // 重置本地分支到远程分支
            String remoteBranch = "origin/" + branchName;
            git.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef(remoteBranch)
                    .call();

            log.info("重置本地分支到远程分支成功: branch={}, remote={}", branchName, remoteBranch);
        } catch (IOException | GitAPIException e) {
            log.error("重置分支失败: branch={}, error={}", branchName, e.getMessage(), e);
            throw new BizException("重置分支失败: " + e.getMessage());
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
    public String cloneFullRepository(String repoUrl, String projectName, String cacheId,
                                      GitCredentials credentials) {
        // 缓存仓库路径 {cachePath}/{projectName}
        String localPath = gitPathBuilder.getCacheRepoPath(projectName);
        File localDir = new File(localPath);

        // 确保父级目录存在
        FileUtil.mkdir(localDir.getParentFile());

        // 如果目录已存在，先清理
        if (localDir.exists()) {
            FileUtil.del(localDir);
        }

        log.info("开始完整克隆仓库（用于缓存）: {} -> {}", repoUrl, localPath);

        try {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    credentials.getUsername(), credentials.getPassword());

            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localDir)
                    .setCredentialsProvider(credentialsProvider)
                    .setCloneAllBranches(true)  // 克隆所有分支
                    .setTimeout(jgitConfig.getCloneTimeoutSeconds());

            // 不使用浅克隆，下载完整历史
            cloneCommand.call().close();

            log.info("完整仓库克隆成功: {}", localPath);
            return localPath;
        } catch (GitAPIException e) {
            log.error("完整克隆仓库失败: {}", e.getMessage(), e);
            // 清理失败的目录
            if (localDir.exists()) {
                FileUtil.del(localDir);
            }
            throw new BizException("完整克隆仓库失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private String getLocalRepoPath(String projectName, String sessionId) {
        return gitPathBuilder.getSessionRepoPath(projectName, sessionId);
    }

    private String getProjectPath(String projectName) {
        return gitPathBuilder.getProjectPath(projectName);
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

    /**
     * 检出符号链接
     */
    private void checkoutSymlink(ObjectLoader loader, File targetFile) throws IOException {
        String linkTarget = new String(loader.getBytes(), StandardCharsets.UTF_8);
        try {
            // 尝试创建符号链接
            Files.createSymbolicLink(targetFile.toPath(), Paths.get(linkTarget));
        } catch (UnsupportedOperationException | IOException e) {
            // Windows可能不支持符号链接，或权限不足，写入普通文件
            log.warn("无法创建符号链接，写入为普通文件: {}", targetFile.getPath());
            Files.write(targetFile.toPath(), loader.getBytes());
        }
    }

    /**
     * 检出普通文件
     */
    private void checkoutRegularFile(ObjectLoader loader, File targetFile, boolean executable) throws IOException {
        if (loader.isLarge()) {
            // 大文件：使用流式处理，避免内存溢出
            try (java.io.InputStream in = loader.openStream();
                 java.io.OutputStream out = Files.newOutputStream(targetFile.toPath())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } else {
            // 小文件：直接写入
            try (java.io.OutputStream out = Files.newOutputStream(targetFile.toPath())) {
                loader.copyTo(out);
            }
        }

        // 设置可执行权限
        if (executable) {
            targetFile.setExecutable(true);
        }
    }

    /**
     * 判断路径是否匹配稀疏检出规则
     */
    private boolean matchesSparseCheckout(String path, Set<String> sparseSet) {
        // 精确匹配
        if (sparseSet.contains(path)) {
            return true;
        }

        // 目录匹配：检查是否在某个目录下
        for (String pattern : sparseSet) {
            if (pattern.endsWith("/")) {
                // 目录模式：path在该目录下
                if (path.startsWith(pattern)) {
                    return true;
                }
            } else {
                // 文件模式：检查是否是该文件的父目录
                if (path.startsWith(pattern + "/")) {
                    return true;
                }
            }
        }

        return false;
    }

}