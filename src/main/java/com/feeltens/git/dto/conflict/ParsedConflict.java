package com.feeltens.git.dto.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 解析后的冲突信息
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedConflict {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 原始内容（包含冲突标记）
     */
    private String originalContent;

    /**
     * 冲突块列表
     */
    private List<ConflictBlock> conflictBlocks;

    /**
     * 非冲突区域内容片段（按顺序）
     */
    private List<String> nonConflictSegments;

    /**
     * 冲突块总数
     */
    private Integer totalConflicts;

}