package com.feeltens.git.vo.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 列表查询git远程仓库 (open api) 出参
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class ListGitRepositoryRespVO implements Serializable {

    private static final long serialVersionUID = 9177792818400025910L;

    /**
     * git远程仓库id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long repositoryId;

    /**
     * git远程仓库名称
     */
    private String repositoryName;

}