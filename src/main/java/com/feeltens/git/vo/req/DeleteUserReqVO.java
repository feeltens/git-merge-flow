package com.feeltens.git.vo.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 删除用户请求
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
public class DeleteUserReqVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 操作人
     */
    private String operator;

}