package com.feeltens.git.vo.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新用户Project权限请求
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
public class UpdateUserProjectPermReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 工程ID列表
     */
    private List<Long> projectIds;

    /**
     * 权限类型: READ-只读, READ_WRITE-读写
     */
    private String permType;

    /**
     * 操作人
     */
    private String operator;

}