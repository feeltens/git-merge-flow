package com.feeltens.git.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * git中间分支的条目表
 *
 * @author feeltens
 * @date 2025-08-17
 *
 * <pre>
 * 表名：git_mix_branch_item
 * </pre>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class GitMixBranchItemDO implements Serializable {

    /**
     * 主键id
     */
    private Long mixBranchItemId;

    /**
     * 中间分支id
     */
    private Long mixBranchId;

    /**
     * 原始分支名称
     */
    private String branchName;

    /**
     * 是否合并的标识，0代表未合并，1代表已合并
     */
    private Integer mergeFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 删除标识: 0代表未删除，>0代表已删除，此时同mix_branch_item_id
     */
    private Long deleted;

    private static final long serialVersionUID = 1L;

}