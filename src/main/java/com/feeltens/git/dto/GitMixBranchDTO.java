package com.feeltens.git.dto;

import com.feeltens.git.entity.GitMixBranchDO;
import com.feeltens.git.entity.GitMixBranchItemDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * git中间分支 dto
 *
 * @author feeltens
 * @date 2025-08-20
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class GitMixBranchDTO extends GitMixBranchDO {

    /**
     * 中间分支item列表
     */
    private List<GitMixBranchItemDO> mixBranchItemList;

}