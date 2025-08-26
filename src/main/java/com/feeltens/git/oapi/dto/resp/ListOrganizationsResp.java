package com.feeltens.git.oapi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;
import java.util.List;

/**
 * ListOrganizations - 查询组织列表 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class ListOrganizationsResp {

    private List<ListOrganizationsRespItem> organizationItemList;

    /*
    [
        {
            "id": "yourOrganizationId",
            "created": "2020-06-04T10:28:03.961Z",
            "updated": "2025-03-01T12:09:05.785Z",
            "name": "tom的团队",
            "description": ""
        }
    ]
    */

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder(toBuilder = true)
    @FieldNameConstants
    public static class ListOrganizationsRespItem {

        /**
         * 组织ID
         */
        private String id;

        /**
         * 组织名称
         */
        private String name;

        /**
         * 组织描述
         */
        private String description;

        private Date createTime;
        private Date updateTime;
    }

}