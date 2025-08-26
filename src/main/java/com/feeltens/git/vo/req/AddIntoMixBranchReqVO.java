package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 添加分支进git中间分支 入参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class AddIntoMixBranchReqVO implements Serializable {

    private static final long serialVersionUID = 2010034278794963284L;

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