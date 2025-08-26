package com.feeltens.git.oapi.dto.resp;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.util.Date;
import java.util.List;

/**
 * ListBranches - 查询分支列表 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class ListBranchesResp {

    private List<ListBranchesResp.BranchItem> branchItemList;

    /*
    [
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
        },
        {
            "commit": {
                "authorEmail": "xxx@163.com",
                "authorName": "feeltens",
                "authoredDate": "2025-08-16T17:34:00+08:00",
                "committedDate": "2025-08-16T17:34:00+08:00",
                "committerEmail": "xxx@163.com",
                "committerName": "feeltens",
                "id": "d1dbab8783d02a1f0828b0821602db904f485660",
                "message": "test1 commit",
                "parentIds": [
                    "d2fd95efaccf64cd944dfee8756ae0ce4a826092"
                ],
                "shortId": "d1dbab87",
                "stats": null,
                "title": "test1 commit"
            },
            "defaultBranch": false,
            "name": "test1",
            "protected": false,
            "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo/tree/test1"
        },
        {
            "commit": {
                "authorEmail": "xxx@163.com",
                "authorName": "tom",
                "authoredDate": "2025-08-17T08:45:26+08:00",
                "committedDate": "2025-08-17T08:45:26+08:00",
                "committerEmail": "xxx@163.com",
                "committerName": "tom",
                "id": "fd4e279b6b3c7c0eef869ff95abf13d9ba5909c6",
                "message": "Merge branch 'refs/heads/test4' into 'refs/heads/test_mix'.  Code review title: GitMergeFlow: merge branch test4 into test_mix.  Code review description: GitMergeFlow: merge branch test4 into test_mix.  Code review Link: https://codeup.aliyun.com/yourOrganizationId/demo-repo/change/7",
                "parentIds": [
                    "2a4b61c10a7763f7dd2c12d1d0b0692a1f0ed055",
                    "78b9c8155b9380eb4b9c78cd0371b587aa755d91"
                ],
                "shortId": "fd4e279b",
                "stats": null,
                "title": "Merge branch 'refs/heads/test4' into 'refs/heads/test_mix'.  Code review title:..."
            },
            "defaultBranch": false,
            "name": "test_mix",
            "protected": false,
            "webUrl": "https://codeup.aliyun.com/yourOrganizationId/demo-repo/tree/test_mix"
        }
    ]
    */

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder(toBuilder = true)
    @FieldNameConstants
    public static class BranchItem {

        /**
         * 分支名称
         */
        private String name;

        private String authorName;
        private String authorEmail;
        private Date committedDate;

        private String webUrl;

        /**
         * 是否是默认分支
         */
        private Boolean defaultBranch;

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

}