package com.feeltens.git.dto.diff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Diff行
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiffLine {

    /**
     * 行号（base）
     */
    private Integer baseLineNumber;

    /**
     * 行号（ours）
     */
    private Integer oursLineNumber;

    /**
     * 行号（theirs）
     */
    private Integer theirsLineNumber;

    /**
     * 行内容
     */
    private String content;

    /**
     * 行状态
     */
    private LineStatus status;

    /**
     * 行状态枚举
     */
    public enum LineStatus {
        UNCHANGED,      // 未改变
        ADDED,          // 新增
        DELETED,        // 删除
        MODIFIED,       // 修改
        CONFLICT        // 冲突
    }

}