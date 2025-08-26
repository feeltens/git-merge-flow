package com.feeltens.git.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 分页查询git工程 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class PageGitProjectRespVO implements Serializable {

    private static final long serialVersionUID = -392601012960923852L;

    /**
     * 工程id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
     * 仓库id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * dev环境 中间分支id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long devMixBranchId;

    /**
     * test环境 中间分支id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long testMixBranchId;

    /**
     * pre环境 中间分支id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long preMixBranchId;

}