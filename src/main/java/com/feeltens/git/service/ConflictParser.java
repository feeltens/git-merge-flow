package com.feeltens.git.service;

import com.feeltens.git.dto.conflict.ConflictBlock;
import com.feeltens.git.dto.conflict.ParsedConflict;

import java.util.List;

/**
 * 冲突解析器接口
 *
 * @author feeltens
 */
public interface ConflictParser {

    /**
     * 解析冲突文件内容
     *
     * @param conflictContent 包含冲突标记的文件内容
     * @param filePath        文件路径
     * @return 解析后的冲突信息
     */
    ParsedConflict parseConflict(String conflictContent, String filePath);

    /**
     * 生成合并后的文件内容
     *
     * @param parsedConflict 解析后的冲突信息
     * @param resolutions    用户的解决方案（按冲突块索引顺序）
     * @return 合并后的完整文件内容
     */
    String generateResolvedContent(ParsedConflict parsedConflict, List<ConflictBlock> resolutions);

    /**
     * 检查内容是否包含冲突标记
     *
     * @param content 文件内容
     * @return 是否包含冲突
     */
    boolean hasConflictMarkers(String content);

}