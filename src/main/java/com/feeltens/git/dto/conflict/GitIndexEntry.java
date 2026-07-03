package com.feeltens.git.dto.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Git索引条目
 * 用于表示Git索引中的stage信息
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitIndexEntry {

    /**
     * 文件路径
     */
    private String path;

    /**
     * Stage (0=正常, 1=base, 2=ours, 3=theirs)
     */
    private int stage;

    /**
     * Blob对象ID
     */
    private String objectId;

    /**
     * 文件模式
     */
    private int fileMode;

    /**
     * 文件长度
     */
    private long length;

    /**
     * 最后修改时间
     */
    private long lastModified;

    /**
     * 是否为ASSUME_VALID
     */
    private boolean isAssumeValid;

    /**
     * 是否为SKIP_WORK_TREE
     */
    private boolean isSkipWorkTree;

}