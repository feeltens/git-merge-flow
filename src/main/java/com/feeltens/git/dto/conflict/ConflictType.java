package com.feeltens.git.dto.conflict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 冲突类型枚举
 *
 * @author feeltens
 */
@Getter
@AllArgsConstructor
public enum ConflictType {

    DEFAULT("default", "标准冲突"),
    ADDED_ADDED("added_added", "双方添加"),
    MODIFIED_DELETED("modified_deleted", "修改删除"),
    DELETED_MODIFIED("deleted_modified", "删除修改"),
    ADDED("added", "文件添加"),
    DELETED("deleted", "文件删除"),
    DELETED_DELETED("deleted_deleted", "双方删除"),
    UNKNOWN("unknown", "未知类型"),
    ;

    private final String code;
    private final String description;

    /**
     * 根据code获取ConflictType
     *
     * @param code 代码
     * @return ConflictType
     */
    public static ConflictType fromCode(String code) {
        for (ConflictType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }

}