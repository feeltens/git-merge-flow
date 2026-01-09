package com.feeltens.git.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冲突解决会话状态枚举
 *
 * @author feeltens
 */
@Getter
@AllArgsConstructor
public enum SessionStatus {

    INITIALIZING("initializing", "初始化中"),
    READY("ready", "就绪"),
    RESOLVING("resolving", "解决中"),
    COMMITTED("committed", "已提交"),
    CANCELLED("cancelled", "已取消"),
    EXPIRED("expired", "已过期"),
    FAILED("failed", "失败");

    private final String code;
    private final String desc;

}