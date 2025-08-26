package com.feeltens.git.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 环境 枚举
 *
 * @author feeltens
 * @date 2025-08-20
 */
public enum EnvEnum {

    DEV("dev", "开发环境"),
    TEST("test", "测试环境"),
    PRE("pre", "预发环境"),
    ;

    private String code;
    private String desc;

    private static final Map<String, EnvEnum> ENV_ENUM_MAP = new ConcurrentHashMap<>();

    static {
        for (EnvEnum envEnum : EnvEnum.values()) {
            ENV_ENUM_MAP.put(envEnum.getCode(), envEnum);
        }
    }

    EnvEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static EnvEnum getByEnvCode(String envCode) {
        return ENV_ENUM_MAP.get(envCode);
    }

}