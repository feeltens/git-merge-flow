package com.feeltens.git.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * do合并分支 结果 DTO
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class DoMergeBranchResultDTO implements Serializable {

    private static final long serialVersionUID = 5376437890916891409L;

    /**
     * 中间分支id
     */
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