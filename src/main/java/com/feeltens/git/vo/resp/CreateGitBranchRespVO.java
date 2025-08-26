package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建git原始分支 出参
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class CreateGitBranchRespVO implements Serializable {

    private static final long serialVersionUID = -3965207468090335568L;

    /**
     * 分支名称
     */
    private String gitBranchName;

}