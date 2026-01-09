package com.feeltens.git.dto.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 冲突文件内容
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictFileContent {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * Base 版本内容（共同祖先）
     */
    private String baseContent;

    /**
     * Ours 版本内容（目标分支）
     */
    private String oursContent;

    /**
     * Theirs 版本内容（源分支）
     */
    private String theirsContent;

    /**
     * 当前合并内容（包含冲突标记）
     */
    private String mergedContent;

    /**
     * 冲突块列表
     */
    private List<ConflictBlock> conflictBlocks;

}