package com.feeltens.git.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * git服务提供方 枚举
 *
 * @author feeltens
 * @date 2025-08-20
 */
public enum GitServiceEnum {

    GITLAB("gitlab", "GitLab"),
    CODEUP("codeup", "CodeUp"),
    ;

    private String code;
    private String desc;

    private static final Map<String, GitServiceEnum> ENUM_MAP = new ConcurrentHashMap<>();

    static {
        for (GitServiceEnum envEnum : GitServiceEnum.values()) {
            ENUM_MAP.put(envEnum.getCode(), envEnum);
        }
    }

    GitServiceEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static GitServiceEnum getByEnvCode(String envCode) {
        return ENUM_MAP.get(envCode);
    }

}