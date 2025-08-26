package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 添加git工程 req
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class AddGitProjectReqVO implements Serializable {

    private static final long serialVersionUID = -8851158647000657995L;

    /**
     * 组织ID
     */
    private String organizationId;

    /**
     * 工程名称
     */
    private String projectName;

    /**
     * 操作人
     */
    private String operator;

}