package com.feeltens.git.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 初始化进度状态
 *
 * @author feeltens
 */
@Getter
@AllArgsConstructor
public enum InitProgressStatus {

    /**
     * 排队中
     */
    QUEUED("排队中", 0),

    /**
     * 克隆仓库中
     */
    CLONING("克隆仓库中", 30),

    /**
     * 合并分支中
     */
    MERGING("合并分支中", 70),

    /**
     * 加载冲突文件中
     */
    LOADING_CONFLICTS("加载冲突文件中", 80),

    /**
     * 就绪
     */
    READY("就绪", 100),

    /**
     * 失败
     */
    FAILED("失败", -1);

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 默认进度值
     */
    private final Integer defaultProgress;

}