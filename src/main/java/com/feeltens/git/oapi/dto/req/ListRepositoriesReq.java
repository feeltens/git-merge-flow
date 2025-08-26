package com.feeltens.git.oapi.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * ListRepositories - 查询代码库列表 入参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class ListRepositoriesReq extends OapiBaseReq {

    /**
     * 搜索关键字，用户模糊匹配代码库路径
     * <pre>
     *     git仓库名称
     * </pre>
     */
    private String search;

}