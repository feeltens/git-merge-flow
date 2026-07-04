package com.feeltens.git.reader;

import com.feeltens.git.dto.conflict.ConflictFileContent;
import com.feeltens.git.dto.conflict.GitIndexEntry;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Git索引读取器
 * 读取Git索引的stage信息，相当于git ls-files --unmerged
 *
 * @author feeltens
 */
@Component
@Slf4j
public class GitIndexReader {

    /**
     * 读取冲突文件的索引信息
     *
     * @param repository Git仓库
     * @param filePath   文件路径
     * @return 索引信息map（stage -> entry）
     * @throws IOException IO异常
     */
    public Map<Integer, GitIndexEntry> readConflictIndex(Repository repository, String filePath)
            throws IOException {
        DirCache dirCache = repository.readDirCache();

        Map<Integer, GitIndexEntry> indexMap = new HashMap<>();

        // 遍历索引，查找该文件的各个stage
        for (int i = 0; i < dirCache.getEntryCount(); i++) {
            DirCacheEntry entry = dirCache.getEntry(i);
            if (entry.getPathString().equals(filePath)) {
                int stage = entry.getStage();
                GitIndexEntry indexEntry = convertToGitIndexEntry(entry);
                indexMap.put(stage, indexEntry);
                log.debug("找到索引条目: path={}, stage={}, objectId={}",
                        filePath, stage, entry.getObjectId());
            }
        }

        return indexMap;
    }

    /**
     * 读取所有冲突文件
     *
     * @param repository Git仓库
     * @return 冲突文件列表
     * @throws IOException IO异常
     */
    public List<String> listConflictFiles(Repository repository) throws IOException {
        DirCache dirCache = repository.readDirCache();
        List<String> conflictFiles = new ArrayList<>();

        for (int i = 0; i < dirCache.getEntryCount(); i++) {
            DirCacheEntry entry = dirCache.getEntry(i);
            if (entry.getStage() != 0) {
                conflictFiles.add(entry.getPathString());
            }
        }

        return conflictFiles;
    }

    /**
     * 从索引读取blob内容
     *
     * @param repository Git仓库
     * @param objectId   Blob对象ID
     * @return Blob内容
     * @throws IOException IO异常
     */
    public String readBlobContent(Repository repository, ObjectId objectId) throws IOException {
        try (ObjectReader reader = repository.newObjectReader()) {
            byte[] bytes = reader.open(objectId).getBytes();
            return new String(bytes, "UTF-8");
        }
    }

    /**
     * 转换DirCacheEntry为GitIndexEntry
     *
     * @param entry DirCacheEntry
     * @return GitIndexEntry
     */
    private GitIndexEntry convertToGitIndexEntry(DirCacheEntry entry) {
        return GitIndexEntry.builder()
                .path(entry.getPathString())
                .stage(entry.getStage())
                .objectId(entry.getObjectId().name())
                .fileMode(entry.getFileMode().getBits())
                .length(entry.getLength())
                .lastModified(entry.getLastModified())
                .isAssumeValid(entry.isAssumeValid())
                .isSkipWorkTree(entry.isSkipWorkTree())
                .build();
    }

    /**
     * 读取三路内容（base、ours、theirs）
     *
     * @param repository Git仓库
     * @param filePath   文件路径
     * @return 冲突文件内容
     * @throws IOException IO异常
     */
    public ConflictFileContent readThreeWayContent(Repository repository, String filePath)
            throws IOException {
        Map<Integer, GitIndexEntry> indexMap = readConflictIndex(repository, filePath);

        // Stage 1: base (共同祖先)
        GitIndexEntry baseEntry = indexMap.get(1);
        String baseContent = null;
        if (baseEntry != null) {
            baseContent = readBlobContent(repository, ObjectId.fromString(baseEntry.getObjectId()));
        }

        // Stage 2: ours (当前分支)
        GitIndexEntry oursEntry = indexMap.get(2);
        String oursContent = null;
        if (oursEntry != null) {
            oursContent = readBlobContent(repository, ObjectId.fromString(oursEntry.getObjectId()));
        }

        // Stage 3: theirs (合并分支)
        GitIndexEntry theirsEntry = indexMap.get(3);
        String theirsContent = null;
        if (theirsEntry != null) {
            theirsContent = readBlobContent(repository, ObjectId.fromString(theirsEntry.getObjectId()));
        }

        // 读取工作目录内容
        Path workFile = Paths.get(repository.getWorkTree().getAbsolutePath(), filePath);
        String mergedContent = "";
        if (Files.exists(workFile)) {
            mergedContent = new String(Files.readAllBytes(workFile), "UTF-8");
        }

        return ConflictFileContent.builder()
                .filePath(filePath)
                .baseContent(baseContent)
                .oursContent(oursContent)
                .theirsContent(theirsContent)
                .mergedContent(mergedContent)
                .build();
    }

}