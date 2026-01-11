package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录响应
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * token值
     */
    private String tokenValue;

    /**
     * token名称
     */
    private String tokenName;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 是否管理员
     */
    private Boolean isAdmin;

}