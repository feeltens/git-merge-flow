package com.feeltens.git.oapi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * ListRepositories - 查询代码库列表 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ListRepositoriesResp implements Serializable {

    private static final long serialVersionUID = 1555903868666952744L;

    private List<ListRepositoriesRespItem> repoItemList;

    /*
    [
        {
            "accessLevel": 40,
            "archived": false,
            "createdAt": "2025-08-16T15:02:59+08:00",
            "creatorId": 138573432565,
            "demoProject": false,
            "encrypted": false,
            "id": 5600997,
            "lastActivityAt": "2025-08-17T08:45:27+08:00",
            "name": "demo-repo",
            "nameWithNamespace": "yourOrganizationId / demo-repo",
            "namespaceId": 1395639,
            "path": "demo-repo",
            "pathWithNamespace": "yourOrganizationId/demo-repo",
            "starCount": 0,
            "starred": false,
            "updatedAt": "2025-08-17T08:45:27+08:00",
            "visibility": "private",
            "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo"
        }
    ]
    */

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    @FieldNameConstants
    public static class ListRepositoriesRespItem {
        /**
         * 仓库ID
         * "id": 5600997
         */
        private Long id;
        /**
         * 仓库名称
         * "name": "demo-repo"
         */
        private String name;
        private String description;
        private Date createdAt;
        private Date updatedAt;
    }

}