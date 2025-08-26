package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 从git中间分支移除源分支 入参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class RemoveFromMixBranchReqVO implements Serializable {

    private static final long serialVersionUID = -1909047268494905937L;

    /**
     * 中间分支id
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