package com.feeltens.git.oapi.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/**
 * DeleteBranch - 删除分支 出参
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@FieldNameConstants
public class DeleteBranchResp {

    /**
     * 分支名称
     */
    private String branchName;

}