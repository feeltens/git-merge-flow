package com.feeltens.git.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 分页查询git中间分支 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class PageMixBranchRespVO implements Serializable {

    private static final long serialVersionUID = 4883747508757300224L;

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
     * 合并总状态 默认为0-未成功；1-成功
     */
    private Integer allMergeFlag;

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

}