package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 重新合并git中间分支 入参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class RemergeMixBranchReqVO implements Serializable {

    private static final long serialVersionUID = 3749300684953255116L;

    /**
     * 中间分支id
     */
    private Long mixBranchId;

    /**
     * 操作人
     */
    private String operator;

}