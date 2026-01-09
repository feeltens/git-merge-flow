package com.feeltens.git.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冲突解决方式枚举
 *
 * @author feeltens
 */
@Getter
@AllArgsConstructor
public enum ResolutionType {

    ACCEPT_OURS("accept_ours", "采用左侧（目标分支）"),
    ACCEPT_THEIRS("accept_theirs", "采用右侧（源分支）"),
    ACCEPT_BOTH("accept_both", "采用双方"),
    MANUAL("manual", "手动编辑");

    private final String code;
    private final String desc;

}