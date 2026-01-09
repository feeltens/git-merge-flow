package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 冲突会话响应
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictSessionVO {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 冲突文件总数
     */
    private Integer totalFiles;

    /**
     * 已解决文件数
     */
    private Integer resolvedFiles;

    /**
     * 源分支
     */
    private String sourceBranch;

    /**
     * 目标分支
     */
    private String targetBranch;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 会话状态
     */
    private String status;

}