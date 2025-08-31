package com.feeltens.git.vo.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 拉取远程分支 req
 *
 * @author feeltens
 * @date 2025-08-19
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
public class PullRemoteBranchReqVO implements Serializable {

    private static final long serialVersionUID = -2575395694650783101L;

    /**
     * git工程id
     */
    private Long gitProjectId;

}