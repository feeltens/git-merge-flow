package com.feeltens.git.mapper;

import com.feeltens.git.entity.GitProjectDO;
import com.feeltens.git.vo.req.PageGitProjectReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GitProjectMapper {

    int insert(GitProjectDO gitProjectDO);

    /**
     * 根据工程id，查询git工程信息
     *
     * @param gitProjectId 工程id
     * @return
     */
    GitProjectDO selectByProjectId(@Param("gitProjectId") Long gitProjectId);

    Long countProject(@Param("req") PageGitProjectReqVO item);

    List<GitProjectDO> pageProject(@Param("req") PageGitProjectReqVO item,
                                   @Param("limitSize") Integer limitSize,
                                   @Param("pageSize") Integer pageSize);

    GitProjectDO queryByProjectName(@Param("projectName") String projectName);

    GitProjectDO queryByProjectId(@Param("projectId") Long projectId);

    int deleteByProjectId(@Param("projectId") Long projectId);

}