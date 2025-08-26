package com.feeltens.git.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 从git中间分支移除源分支 出参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class RemoveFromMixBranchRespVO implements Serializable {

    private static final long serialVersionUID = 8736276804358365385L;

    /**
     * 中间分支id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long mixBranchId;

    /**
     * 合并状态
     * true代表成功；false代表失败
     */
    private Boolean flag;

    /**
     * 自动集成失败时，用户手动处理冲突来集成的shell命令 （前端展示以\n换行）
     */
    private String userMergeShell;

}