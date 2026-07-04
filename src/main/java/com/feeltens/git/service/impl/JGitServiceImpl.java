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
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
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
import java.util.Arrays;
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

    @Override
    public String cloneRepository(String repoUrl, String projectName, String sessionId,
                                  GitCredentials credentials, String sourceBranch, String targetBranch) {
        String localPath = getLocalRepoPath(projectName, sessionId);
        File localDir = new File(localPath);

        // 如果目录已存在，先清理
        if (localDir.exists()) {
            FileUtil.del(localDir);
        }

        log.info("开始克隆仓库: {} -> {}, 分支: [{}, {}]", repoUrl, localPath, sourceBranch, targetBranch);

        try {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    credentials.getUsername(), credentials.getPassword());

            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(localDir)
                    .setCredentialsProvider(credentialsProvider)
                    .setCloneAllBranches(false)
                    .setBranchesToClone(Arrays.asList(
                            "refs/heads/" + sourceBranch,
                            "refs/heads/" + targetBranch
                    ))
                    .setTimeout(jgitConfig.getCloneTimeoutSeconds());

            // 启用浅克隆优化
            if (jgitConfig.isEnableShallowClone()) {
                cloneCommand.setNoTags();
                log.info("启用浅克隆优化（禁用标签下载）");
            }

            cloneCommand.call().close();

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
    public String cloneRepositoryWithSparseCheckout(String repoUrl, String projectName, String sessionId,
                                                    GitCredentials credentials, String sourceBranch,
                                                    String targetBranch, List<String> sparseFiles) {
        // 如果没有指定稀疏文件列表，或列表为空，降级到完整克隆
        if (sparseFiles == null || sparseFiles.isEmpty()) {
            log.info("稀疏文件列表为空，降级到完整克隆");
            return cloneRepository(repoUrl, projectName, sessionId, credentials, sourceBranch, targetBranch);
        }

        String localPath = getLocalRepoPath(projectName, sessionId);
        File localDir = new File(localPath);

        // 如果目录已存在，先清理
        if (localDir.exists()) {
            FileUtil.del(localDir);
        }

        log.info("开始稀疏检出克隆仓库: {} -> {}, 分支: [{}, {}], 差异文件数: {}",
                repoUrl, localPath, sourceBranch, targetBranch, sparseFiles.size());

        Git git = null;
        try {
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    credentials.getUsername(), credentials.getPassword());

            // 阶段1: 初始化空仓库
            git = Git.init()
                    .setDirectory(localDir)
                    .call();

            Repository repository = git.getRepository();
            org.eclipse.jgit.lib.StoredConfig config = repository.getConfig();

            // 阶段2: 配置远程仓库
            config.setString("remote", "origin", "url", repoUrl);
            config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");

            // 启用稀疏检出配置（用于标识和后续可能的Git命令）
            config.setBoolean("core", null, "sparseCheckout", true);
            config.save();

            // 写入sparse-checkout文件
            File sparseCheckoutFile = new File(localDir, ".git/info/sparse-checkout");
            sparseCheckoutFile.getParentFile().mkdirs();

            // 构建稀疏检出模式列表
            List<String> sparsePatterns = buildSparsePatterns(sparseFiles);
            Files.write(sparseCheckoutFile.toPath(), sparsePatterns, StandardCharsets.UTF_8);
            log.info("稀疏检出配置已写入，模式数: {}", sparsePatterns.size());

            // 阶段3: Fetch远程分支（只拉取对象，不检出工作目录）
            FetchCommand fetchCommand = git.fetch()
                    .setRemote("origin")
                    .setRefSpecs(
                            new RefSpec("+refs/heads/" + sourceBranch + ":refs/remotes/origin/" + sourceBranch),
                            new RefSpec("+refs/heads/" + targetBranch + ":refs/remotes/origin/" + targetBranch)
                    )
                    .setCredentialsProvider(credentialsProvider)
                    .setTimeout(jgitConfig.getCloneTimeoutSeconds());

            // 启用浅克隆优化
            if (jgitConfig.isEnableShallowClone()) {
                fetchCommand.setThin(true);
                log.info("启用浅克隆优化（稀疏传输）");
            }

            fetchCommand.call();
            log.info("Fetch完成，对象已下载");

            // 阶段4: 创建本地分支（不检出）
            ObjectId targetCommitId = repository.resolve("origin/" + targetBranch);
            ObjectId sourceCommitId = repository.resolve("origin/" + sourceBranch);

            if (targetCommitId == null) {
                throw new BizException("目标分支不存在: " + targetBranch);
            }
            if (sourceCommitId == null) {
                throw new BizException("源分支不存在: " + sourceBranch);
            }

            // 创建目标分支引用
            RefUpdate targetRefUpdate = repository.updateRef("refs/heads/" + targetBranch);
            targetRefUpdate.setNewObjectId(targetCommitId);
            targetRefUpdate.setRefLogMessage("branch: Created from origin/" + targetBranch, false);
            RefUpdate.Result targetResult = targetRefUpdate.update();

            if (targetResult != RefUpdate.Result.NEW && targetResult != RefUpdate.Result.FORCED) {
                throw new BizException("创建目标分支失败: " + targetResult);
            }

            // 创建源分支引用（merge时需要）
            RefUpdate sourceRefUpdate = repository.updateRef("refs/heads/" + sourceBranch);
            sourceRefUpdate.setNewObjectId(sourceCommitId);
            sourceRefUpdate.setRefLogMessage("branch: Created from origin/" + sourceBranch, false);
            RefUpdate.Result sourceResult = sourceRefUpdate.update();

            if (sourceResult != RefUpdate.Result.NEW && sourceResult != RefUpdate.Result.FORCED) {
                throw new BizException("创建源分支失败: " + sourceResult);
            }

            // 设置HEAD指向目标分支
            RefUpdate headUpdate = repository.updateRef(org.eclipse.jgit.lib.Constants.HEAD);
            headUpdate.link("refs/heads/" + targetBranch);
            log.info("分支创建成功: target={}, source={}", targetBranch, sourceBranch);

            // 阶段5: 稀疏检出文件到工作目录
            Set<String> sparseSet = new HashSet<>(sparseFiles);
            int checkedOutCount = checkoutSparseFiles(repository, targetCommitId, localDir, sparseFiles);
            log.info("工作目录检出完成，成功检出 {} 个文件", checkedOutCount);

            // 阶段6: 构建完整索引（关键：包含所有文件，但标记非稀疏文件）
            int indexEntryCount = buildCompleteIndex(repository, targetCommitId, localDir, sparseSet);
            log.info("索引构建完成，索引条目数: {}", indexEntryCount);

            // 阶段7: 验证仓库完整性
            verifyRepositoryIntegrity(repository);

            git.close();

            int actualFileCount = countFilesInWorkingDirectory(localDir);
            log.info("稀疏检出克隆成功: {}, 工作目录文件数: {}, 索引条目数: {}",
                    localPath, actualFileCount, indexEntryCount);
            return localPath;

        } catch (Exception e) {
            log.error("稀疏检出克隆失败: {}, 降级到完整克隆", e.getMessage(), e);
            // 清理失败的目录
            if (git != null) {
                git.close();
            }
            if (localDir.exists()) {
                FileUtil.del(localDir);
            }
            // 降级到完整克隆
            return cloneRepository(repoUrl, projectName, sessionId, credentials, sourceBranch, targetBranch);
        }
    }

    /**
     * 构建稀疏检出模式列表
     */
    private List<String> buildSparsePatterns(List<String> sparseFiles) {
        List<String> patterns = new ArrayList<>();
        Set<String> added = new HashSet<>();

        for (String file : sparseFiles) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            // 添加文件本身
            if (added.add(file)) {
                patterns.add(file);
            }

            // 添加父目录（确保目录结构存在）
            int lastSlash = file.lastIndexOf('/');
            if (lastSlash > 0) {
                String dir = file.substring(0, lastSlash);
                String dirPattern = dir + "/";
                if (added.add(dirPattern)) {
                    patterns.add(dirPattern);
                }
            }
        }

        return patterns;
    }

    /**
     * 稀疏检出文件到工作目录
     */
    private int checkoutSparseFiles(Repository repository, ObjectId commitId, File localDir,
                                    List<String> sparseFiles) throws IOException {
        int checkedOutCount = 0;

        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(commitId);

            for (String filePath : sparseFiles) {
                // 跳过目录标记
                if (filePath.endsWith("/")) {
                    continue;
                }

                try (TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, commit.getTree())) {
                    if (treeWalk != null) {
                        FileMode fileMode = treeWalk.getFileMode(0);

                        // 只处理文件（不是子模块、gitlink等）
                        if (fileMode.equals(FileMode.REGULAR_FILE) ||
                                fileMode.equals(FileMode.EXECUTABLE_FILE) ||
                                fileMode.equals(FileMode.SYMLINK)) {

                            ObjectId blobId = treeWalk.getObjectId(0);
                            ObjectLoader loader = repository.open(blobId);

                            File targetFile = new File(localDir, filePath);
                            targetFile.getParentFile().mkdirs();

                            // 根据文件类型处理
                            if (fileMode.equals(FileMode.SYMLINK)) {
                                // 符号链接
                                checkoutSymlink(loader, targetFile);
                            } else {
                                // 普通文件或可执行文件
                                checkoutRegularFile(loader, targetFile, fileMode.equals(FileMode.EXECUTABLE_FILE));
                            }

                            checkedOutCount++;
                            log.debug("检出文件: {} (mode={})", filePath, fileMode);
                        } else {
                            log.warn("路径不是普通文件，跳过: {} (mode={})", filePath, fileMode);
                        }
                    } else {
                        log.warn("文件在目标分支中不存在，跳过: {}", filePath);
                    }
                } catch (Exception e) {
                    log.error("检出文件失败: {}, error: {}", filePath, e.getMessage());
                    // 继续处理其他文件
                }
            }
        }

        return checkedOutCount;
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
     * 构建完整索引（包含所有文件，但标记非稀疏文件）
     */
    private int buildCompleteIndex(Repository repository, ObjectId commitId, File localDir,
                                   Set<String> sparseSet) throws IOException {
        DirCache dirCache = lockDirCacheWithRetry(repository, 3);
        DirCacheBuilder builder = dirCache.builder();

        try (RevWalk revWalk = new RevWalk(repository);
             TreeWalk treeWalk = new TreeWalk(repository)) {

            RevCommit commit = revWalk.parseCommit(commitId);
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);

            int entryCount = 0;

            while (treeWalk.next()) {
                String path = treeWalk.getPathString();
                FileMode fileMode = treeWalk.getFileMode(0);

                // 跳过子模块和gitlink
                if (fileMode.equals(FileMode.GITLINK)) {
                    log.debug("跳过子模块: {}", path);
                    continue;
                }

                // 创建索引条目
                DirCacheEntry entry = new DirCacheEntry(path);
                entry.setFileMode(fileMode);
                entry.setObjectId(treeWalk.getObjectId(0));

                // 判断是否在稀疏检出列表中
                boolean inSparseCheckout = matchesSparseCheckout(path, sparseSet);

                if (inSparseCheckout) {
                    // 在稀疏检出列表中：设置实际的文件信息
                    File workingFile = new File(localDir, path);
                    if (workingFile.exists() && workingFile.isFile()) {
                        entry.setLength(workingFile.length());
                        entry.setLastModified(workingFile.lastModified());
                    }
                } else {
                    // 不在稀疏检出列表中：设置ASSUME_VALID标志
                    // 注意：JGit 5.x不完全支持SKIP_WORKTREE，使用ASSUME_VALID作为替代
                    entry.setAssumeValid(true);
                }

                builder.add(entry);
                entryCount++;
            }

            builder.commit();
            return entryCount;

        } finally {
            dirCache.unlock();
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

    /**
     * 带重试机制的DirCache锁定
     */
    private DirCache lockDirCacheWithRetry(Repository repository, int maxRetries) throws IOException {
        int retries = 0;
        while (retries < maxRetries) {
            try {
                return repository.lockDirCache();
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().contains("locked") && retries < maxRetries - 1) {
                    log.warn("DirCache已锁定，等待重试... ({}/{})", retries + 1, maxRetries);
                    try {
                        Thread.sleep(100 * (retries + 1));  // 递增等待时间
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("等待DirCache锁被中断", ie);
                    }
                    retries++;
                } else {
                    throw e;
                }
            }
        }
        throw new IOException("无法获取DirCache锁，已重试" + maxRetries + "次");
    }

    /**
     * 验证仓库完整性
     */
    private void verifyRepositoryIntegrity(Repository repository) throws IOException {
        try (ObjectReader reader = repository.newObjectReader()) {
            // 验证HEAD指向的commit是否存在
            ObjectId headId = repository.resolve(org.eclipse.jgit.lib.Constants.HEAD);
            if (headId == null) {
                throw new IOException("HEAD未设置");
            }

            // 验证commit对象是否可读
            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit commit = revWalk.parseCommit(headId);

                // 验证树对象是否可读
                ObjectId treeId = commit.getTree().getId();
                reader.open(treeId);

                log.debug("仓库完整性验证通过");
            }
        }
    }

    /**
     * 统计工作目录中的文件数
     */
    private int countFilesInWorkingDirectory(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            return 0;
        }

        int count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(".git")) {
                    continue;
                }
                if (file.isFile()) {
                    count++;
                } else if (file.isDirectory()) {
                    count += countFilesInWorkingDirectory(file);
                }
            }
        }
        return count;
    }

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

                // 【新增】清理merge过程中可能检出的非稀疏文件
                if (jgitConfig.isEnableSparseCheckout()) {
                    try {
                        cleanupNonSparseFiles(localRepoPath);
                    } catch (Exception e) {
                        log.warn("清理非稀疏文件失败: {}", e.getMessage());
                        // 不影响主流程
                    }
                }

                return MergeResult.builder()
                        .success(false)
                        .hasConflicts(true)
                        .conflictFiles(conflictFiles)
                        .statusMessage("合并存在冲突，需要手动解决")
                        .build();
            } else if (status.isSuccessful()) {
                // 【新增】成功合并后也清理非稀疏文件
                if (jgitConfig.isEnableSparseCheckout()) {
                    try {
                        cleanupNonSparseFiles(localRepoPath);
                    } catch (Exception e) {
                        log.warn("清理非稀疏文件失败: {}", e.getMessage());
                    }
                }

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