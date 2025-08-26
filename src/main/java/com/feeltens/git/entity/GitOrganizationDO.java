package com.feeltens.git.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * git组织
 *
 * @author feeltens
 * @date 2025-08-17
 *
 * <pre>
 * 表名：git_organization
 * </pre>
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class GitOrganizationDO implements Serializable {

    /**
     * 组织自增id
     */
    private Long orgInnerId;

    /**
     * 组织id(git服务平台的组织id)
     */
    private String organizationId;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 组织描述
     */
    private String description;

    /**
     * webUrl
     */
    private String webUrl;

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
     * 删除标识: 0代表未删除，>0代表已删除，此时同org_inner_id
     */
    private Long deleted;

    private static final long serialVersionUID = 1L;

}