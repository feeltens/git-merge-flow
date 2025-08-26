package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 删除git分支 入参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class DeleteGitBranchReqVO implements Serializable {

    private static final long serialVersionUID = 4409069020939744919L;

    /**
     * 工程id
     * <pre>
     *     projectId、mixBranchId不能同时为空
     * </pre>
     */
    private Long projectId;
    /**
     * 中间分支id
     * <pre>
     *     projectId、mixBranchId不能同时为空
     * </pre>
     */
    private Long mixBranchId;

    /**
     * 源分支名称列表
     */
    private List<String> gitBranchNameList;

    /**
     * 操作人
     */
    private String operator;

}