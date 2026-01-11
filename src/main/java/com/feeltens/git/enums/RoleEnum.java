package com.feeltens.git.enums;

/**
 * 角色枚举
 *
 * @author feeltens
 * @date 2026-01-11
 */
public enum RoleEnum {

    ADMIN("ADMIN", "管理员"),
    USER("USER", "普通用户"),
    ;

    private final String code;
    private final String desc;

    RoleEnum(String code, String desc) {
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