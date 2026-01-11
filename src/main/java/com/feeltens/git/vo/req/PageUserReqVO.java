package com.feeltens.git.vo.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询用户请求
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
public class PageUserReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名 (模糊查询)
     */
    private String username;

    /**
     * 昵称 (模糊查询)
     */
    private String nickname;

    /**
     * 状态
     */
    private Integer status;

}