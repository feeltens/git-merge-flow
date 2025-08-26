package com.feeltens.git.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * git工程
 *
 * @author feeltens
 * @date 2025-08-17
 *
 * <pre>
 * 表名：git_project
 * </pre>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class GitProjectDO implements Serializable {

    /**
     * 工程id (主键自增id)
     */
    private Long projectId;

    /**
     * 工程名称
     */
    private String projectName;

    /**
     * 仓库地址
     */
    private String repositoryUrl;

    /**
     * 组织id
     */
    private String organizationId;

    /**
     * 仓库id (git服务平台的仓库id)
     * <pre>
     *      gitlab 的 projectId
     *      codeup 的 repositoryId
     * </pre>
     */
    private Long repositoryId;

    /**
     * 工程仓库用户名
     */
    private String projectUsername;

    /**
     * 工程仓库邮箱
     */
    private String projectEmail;

    /**
     * 代码库默认分支
     */
    private String defaultBranch;

    /**
     * webUrl
     */
    private String webUrl;

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
     * 删除标识: 0代表未删除，>0代表已删除，此时同project_id
     */
    private Long deleted;

    private static final long serialVersionUID = 1L;

}