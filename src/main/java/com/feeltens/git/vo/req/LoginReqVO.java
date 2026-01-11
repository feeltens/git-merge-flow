package com.feeltens.git.vo.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
public class LoginReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}