package com.feeltens.git.dto.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 合并结果
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeResult {

    /**
     * 是否成功（无冲突）
     */
    private Boolean success;

    /**
     * 是否有冲突
     */
    private Boolean hasConflicts;

    /**
     * 冲突文件列表
     */
    private List<String> conflictFiles;

    /**
     * 合并状态描述
     */
    private String statusMessage;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

}