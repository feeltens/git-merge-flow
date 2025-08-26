package com.feeltens.git.oapi.dto.resp;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;

/**
 * GetBranch - 查询分支信息 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class GetBranchResp {

    /*
    {
        "commit": {
            "authorEmail": "xxx@163.com",
            "authorName": "tom",
            "authoredDate": "2025-08-17T08:39:15+08:00",
            "committedDate": "2025-08-17T08:39:15+08:00",
            "committerEmail": "xxx@163.com",
            "committerName": "tom",
            "id": "75dce4e7ad5e6c312e939cae49c350477afafe7e",
            "message": "Merge branch 'refs/heads/test4' into 'refs/heads/master'.  Code review title: GitMergeFlow: merge branch test4 into master.  Code review description: GitMergeFlow: merge branch test4 into master.  Code review Link: https://codeup.aliyun.com/yourOrganizationId/demo-repo/change/6",
            "parentIds": [
                "e7a6c796a0a1b0b532b002dd5de411a52b8f3e50",
                "e4bd0f056c4d367d9dfd4a24ce04219bb48ef6bb"
            ],
            "shortId": "75dce4e7",
            "stats": null,
            "title": "Merge branch 'refs/heads/test4' into 'refs/heads/master'.  Code review title: c..."
        },
        "defaultBranch": true,
        "name": "master",
        "protected": false,
        "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo/tree/master"
    }
    */

    /**
     * 分支名称
     */
    private String name;

    private String authorName;
    private String authorEmail;
    private Date committedDate;

    private JSONObject commit;

    private String webUrl;

}