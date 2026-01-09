package com.feeltens.git.vo.req;

import lombok.Data;

/**
 * 提交冲突解决请求
 *
 * @author feeltens
 */
@Data
public class CommitConflictReqVO {

    /**
     * 提交信息
     */
    private String commitMessage;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 作者邮箱
     */
    private String authorEmail;

}