package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交结果响应
 *
 * @author feeltens
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitResultVO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 提交信息
     */
    private String message;

    /**
     * 提交 SHA
     */
    private String commitSha;

    /**
     * 目标分支
     */
    private String targetBranch;

}