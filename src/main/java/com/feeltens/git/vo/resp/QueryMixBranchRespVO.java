package com.feeltens.git.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询git中间分支详情 出参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class QueryMixBranchRespVO implements Serializable {

    private static final long serialVersionUID = 7182412979068171240L;

    /**
     * 中间分支id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long projectId;

    /**
     * 合并总状态
     * 默认为0-未成功； （前端展示：自动集成失败，手动处理冲突）
     * 1-成功  （前端展示：自动集成成功）
     */
    private Integer allMergeFlag;

    /**
     * 自动集成失败时，用户手动处理冲突来集成的shell命令 （前端展示以\n换行）
     */
    private String userMergeShell;

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
     * （前端展示：集成人）
     */
    private String updateBy;

    /**
     * 更新时间
     * （前端展示：集成时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 当前集成分支列表
     */
    private List<GitBranchRespVO> mixedBranchVoList;

    /**
     * 待集成分支列表
     */
    private List<GitBranchRespVO> toMixBranchVoList;

}