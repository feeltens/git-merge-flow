package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 列表查询git远程仓库名称 (open api) req
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ListGitRepositoryNameReqVO implements Serializable {

    private static final long serialVersionUID = 4477503597514506292L;

    /**
     * 组织id(git服务平台的组织id)
     */
    private String organizationId;

}