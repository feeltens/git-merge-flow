package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 添加git组织 req
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class AddGitOrganizationReqVO implements Serializable {

    private static final long serialVersionUID = -1355639302776049535L;

    /**
     * git组织id
     */
    private String organizationId;

    /**
     * 操作人
     */
    private String operator;

}