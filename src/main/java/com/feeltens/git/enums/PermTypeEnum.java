package com.feeltens.git.enums;

/**
 * 权限类型枚举
 *
 * @author feeltens
 * @date 2026-01-11
 */
public enum PermTypeEnum {

    READ("READ", "只读"),
    READ_WRITE("READ_WRITE", "读写"),
    ;

    private final String code;
    private final String desc;

    PermTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}