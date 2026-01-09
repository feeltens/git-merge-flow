package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 冲突文件信息响应
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictFileVO {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 冲突块数量
     */
    private Integer conflictCount;

    /**
     * 已解决冲突数
     */
    private Integer resolvedCount;

    /**
     * 是否完全解决
     */
    private Boolean fullyResolved;

    /**
     * 文件类型（用于语法高亮）
     */
    private String fileType;

}