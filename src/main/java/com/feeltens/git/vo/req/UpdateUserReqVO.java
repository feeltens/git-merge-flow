package com.feeltens.git.vo.req;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新用户请求
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
public class UpdateUserReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 角色编码列表
     */
    private List<String> roleCodes;

    /**
     * 项目权限ID列表（普通用户）
     */
    private List<Long> projectIds;

    /**
     * 操作人
     */
    private String operator;

}