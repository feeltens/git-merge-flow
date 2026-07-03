package com.feeltens.git.dto.diff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 三路Diff视图对象
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreeWayDiffVO {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * Base版本内容
     */
    private String baseContent;

    /**
     * Ours版本内容
     */
    private String oursContent;

    /**
     * Theirs版本内容
     */
    private String theirsContent;

    /**
     * Diff块列表
     */
    private List<DiffBlock> diffBlocks;

    /**
     * 总行数（base）
     */
    private Integer baseTotalLines;

    /**
     * 总行数（ours）
     */
    private Integer oursTotalLines;

    /**
     * 总行数（theirs）
     */
    private Integer theirsTotalLines;

}