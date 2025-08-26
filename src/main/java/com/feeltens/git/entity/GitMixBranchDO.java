package com.feeltens.git.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * git中间分支
 *
 * @author feeltens
 * @date 2025-08-17
 *
 * <pre>
 * 表名：git_mix_branch
 * </pre>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class GitMixBranchDO implements Serializable {

    /**
     * 中间分支id
     */
    private Long mixBranchId;

    /**
     * 中间分支名称
     */
    private String mixBranchName;

    /**
     * 环境：dev开发环境；test测试环境；pre预发环境
     */
    private String env;

    /**
     * 工程id
     */
    private Long projectId;

    /**
     * 合并总状态 默认为0-未成功；1-成功
     */
    private Integer allMergeFlag;

    /**
     * （git服务平台的）合并请求的id
     */
    private Long mergeRequestId;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标识: 0代表未删除，>0代表已删除，此时同mix_branch_id
     */
    private Long deleted;

    /**
     * 中间分支ext扩展信息
     */
    private String mixBranchExt;

    private static final long serialVersionUID = 1L;

}