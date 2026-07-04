package com.feeltens.git.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 克隆状态枚举
 *
 * @author feeltens
 */
@Getter
@AllArgsConstructor
public enum CloneStatus {

    /**
     * 未克隆
     */
    NOT_CLONED("NOT_CLONED", "未克隆"),

    /**
     * 克隆中
     */
    CLONING("CLONING", "克隆中"),

    /**
     * 已克隆
     */
    CLONED("CLONED", "已克隆"),

    /**
     * 克隆失败
     */
    CLONE_FAILED("CLONE_FAILED", "克隆失败"),
    ;

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String desc;

}