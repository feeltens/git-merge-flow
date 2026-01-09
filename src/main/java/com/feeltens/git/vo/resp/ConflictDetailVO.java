package com.feeltens.git.vo.resp;

import com.feeltens.git.dto.conflict.ConflictBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 冲突详情响应
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictDetailVO {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 左侧内容（目标分支 Ours）
     */
    private String oursContent;

    /**
     * 右侧内容（源分支 Theirs）
     */
    private String theirsContent;

    /**
     * 基础版本内容
     */
    private String baseContent;

    /**
     * 当前编辑内容（包含冲突标记或已解决内容）
     */
    private String currentContent;

    /**
     * 冲突块列表
     */
    private List<ConflictBlock> conflictBlocks;

    /**
     * 文件类型（用于语法高亮）
     */
    private String fileType;

    /**
     * 是否已解决
     */
    private Boolean resolved;

    /**
     * 源分支名称
     */
    private String sourceBranch;

    /**
     * 目标分支名称
     */
    private String targetBranch;

}