package com.feeltens.git.oapi.dto.resp;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * CreateBranch - 创建分支 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class CreateBranchResp {

    /*
    {
        "commit": {
            "authorEmail": "xxx@163.com",
            "authorName": "feeltens",
            "authoredDate": "2025-08-17T13:11:44+08:00",
            "committedDate": "2025-08-17T13:11:44+08:00",
            "committerEmail": "xxx@163.com",
            "committerName": "feeltens",
            "id": "9e32480116217520b6817e54ad47b8e3aeee3c90",
            "message": "init project\n",
            "parentIds": [],
            "shortId": "9e324801",
            "stats": null,
            "title": "init project"
        },
        "defaultBranch": false,
        "name": "demo1",
        "protected": false,
        "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-code-group/demo-git/tree/demo1"
    }
    */

    /**
     * 分支名称
     */
    private String name;

    private String authorName;
    private String authorEmail;
    private Date committedDate;
    private String webUrl;

    private JSONObject commit;

    /**
     * 最近一次提交时间
     */
    private Date lastCommitTime;

    /**
     * 最近一次提交人
     */
    private String lastCommitUser;

    /**
     * 最近一次提交人邮箱
     */
    private String lastCommitEmail;

    /**
     * 最近一次提交ID
     */
    private String lastCommitId;

    /**
     * 最近一次提交shortID
     */
    private String lastCommitShortId;

    /**
     * 最近一次提交内容
     */
    private String lastCommitMessage;

}