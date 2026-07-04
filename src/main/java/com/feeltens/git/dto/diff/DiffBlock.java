package com.feeltens.git.dto.diff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Diff块
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffBlock {

    /**
     * 块类型
     */
    private BlockType type;

    /**
     * 起始行号（base）
     */
    private Integer baseStartLine;

    /**
     * 结束行号（base）
     */
    private Integer baseEndLine;

    /**
     * 起始行号（ours）
     */
    private Integer oursStartLine;

    /**
     * 结束行号（ours）
     */
    private Integer oursEndLine;

    /**
     * 起始行号（theirs）
     */
    private Integer theirsStartLine;

    /**
     * 结束行号（theirs）
     */
    private Integer theirsEndLine;

    /**
     * 行列表
     */
    private List<DiffLine> lines;

    /**
     * 块类型枚举
     */
    public enum BlockType {
        UNCHANGED,      // 未改变
        ADDED,          // 新增
        DELETED,        // 删除
        MODIFIED,       // 修改
        CONFLICT        // 冲突
    }

}