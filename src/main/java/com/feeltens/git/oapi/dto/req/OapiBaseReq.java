package com.feeltens.git.oapi.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OapiBaseReq {

    /**
     * codeup api 基础url
     */
    private String baseUrl;

    /**
     * codeup api 访问令牌
     */
    private String accessToken;

    /**
     * 组织ID
     * <pre>
     *     codeup 调用 open api 时，需要传入 organizationId；
     *     gitlab 不需要该字段
     * </pre>
     */
    private String organizationId;

}