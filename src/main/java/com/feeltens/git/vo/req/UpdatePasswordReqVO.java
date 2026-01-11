package com.feeltens.git.vo.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
public class UpdatePasswordReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID (管理员修改他人密码时使用)
     */
    private Long userId;

    /**
     * 旧密码 (用户修改自己密码时使用)
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 操作人
     */
    private String operator;

}