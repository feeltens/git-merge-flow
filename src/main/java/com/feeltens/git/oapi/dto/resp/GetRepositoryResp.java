package com.feeltens.git.oapi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * GetRepository - 查询代码库 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class GetRepositoryResp {

    /*
    {
        "accessLevel": "40",
        "allowPush": false,
        "archived": false,
        "cloneDownloadControlGray": true,
        "createdAt": "2025-08-16T15:02:59+08:00",
        "creatorId": 138573432565,
        "defaultBranch": "master",
        "demoProject": false,
        "enableCloneDownloadControl": true,
        "forkCount": 0,
        "httpUrlToRepo": "https://codeup.aliyun.com/yourOrganizationId/demo-repo.git",
        "id": 5600997,
        "lastActivityAt": "2025-08-17T08:45:27+08:00",
        "mergeRequestsEnabled": true,
        "name": "demo-repo",
        "nameWithNamespace": "yourOrganizationId / demo-repo",
        "namespace": {
            "createdAt": "2024-07-01T11:25:49+08:00",
            "description": "",
            "id": 1395639,
            "name": "yourOrganizationId",
            "ownerId": 138573432565,
            "path": "yourOrganizationId",
            "updatedAt": "2024-07-01T11:25:49+08:00",
            "visibility": "private"
        },
        "openCloneDownloadControl": false,
        "owner": {
            "id": 138573432565,
            "webUrl": ""
        },
        "path": "demo-repo",
        "pathWithNamespace": "yourOrganizationId/demo-repo",
        "permissions": {
            "groupAccess": null,
            "projectAccess": null
        },
        "projectType": 1,
        "sshUrlToRepo": "git@codeup.aliyun.com:yourOrganizationId/demo-repo.git",
        "starCount": 0,
        "tagList": [],
        "updatedAt": "2025-08-17T08:45:27+08:00",
        "visibility": "private",
        "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo",
        "wikiEnabled": true
    }
    */

    /**
     * 仓库id
     */
    private Long id;
    /**
     * 仓库名称
     * "name": "demo-repo"
     */
    private String name;
    /**
     * "defaultBranch": "master"
     */
    private String defaultBranch;
    private Date createdAt;
    private Date updatedAt;
    /**
     * "httpUrlToRepo": "https://codeup.aliyun.com/yourOrganizationId/demo-repo.git"
     */
    private String httpUrlToRepo;
    /**
     * "sshUrlToRepo": "git@codeup.aliyun.com:yourOrganizationId/demo-repo.git"
     */
    private String sshUrlToRepo;
    /**
     * "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo"
     */
    private String webUrl;

}