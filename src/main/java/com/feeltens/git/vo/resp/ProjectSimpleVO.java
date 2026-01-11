package com.feeltens.git.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 项目简单信息VO（用于权限分配）
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSimpleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

}