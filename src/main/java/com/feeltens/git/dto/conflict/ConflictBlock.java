package com.feeltens.git.dto.conflict;

import com.feeltens.git.enums.ResolutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冲突块
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictBlock {

    /**
     * 冲突块索引
     */
    private Integer blockIndex;

    /**
     * 起始行号（在合并文件中）
     */
    private Integer startLine;

    /**
     * 结束行号（在合并文件中）
     */
    private Integer endLine;

    /**
     * Ours 版本内容（目标分支）
     */
    private String oursContent;

    /**
     * Theirs 版本内容（源分支）
     */
    private String theirsContent;

    /**
     * Base 版本内容（共同祖先，如果有）
     */
    private String baseContent;

    /**
     * 是否已解决
     */
    private Boolean resolved;

    /**
     * 解决后的内容
     */
    private String resolvedContent;

    /**
     * 解决方式
     */
    private ResolutionType resolutionType;

}