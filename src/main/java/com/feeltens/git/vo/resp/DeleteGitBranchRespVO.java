package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 删除git分支 出参
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class DeleteGitBranchRespVO implements Serializable {

    private static final long serialVersionUID = 3441469514129359846L;

    private List<String> successGitBranchNameList;

}