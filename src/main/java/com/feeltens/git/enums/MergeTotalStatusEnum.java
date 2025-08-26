package com.feeltens.git.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 合并总状态 枚举
 *
 * @author feeltens
 * @date 2025-08-20
 */
public enum MergeTotalStatusEnum {

    CHECKING(0, "正在校验中"),
    CAN_NOT_BE_MERGED(1, "无法合并"),
    CAN_BE_MERGED(2, "可以合并"),
    MERGED(3, "已合并"),
    ;

    private Integer status;
    private String desc;

    private static final Map<Integer, MergeTotalStatusEnum> ENV_ENUM_MAP = new ConcurrentHashMap<>();

    static {
        for (MergeTotalStatusEnum envEnum : MergeTotalStatusEnum.values()) {
            ENV_ENUM_MAP.put(envEnum.getStatus(), envEnum);
        }
    }

    MergeTotalStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public static MergeTotalStatusEnum getByStatus(Integer status) {
        return ENV_ENUM_MAP.get(status);
    }

}