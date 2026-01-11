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

    /**
     * 查询所有项目
     */
    List<GitProjectDO> selectAll();

    /**
     * 根据用户ID查询有权限的项目
     */
    List<GitProjectDO> selectByUserId(@Param("userId") Long userId);

    /**
     * 按用户权限统计项目数量
     */
    Long countProjectByUserId(@Param("req") PageGitProjectReqVO req,
                              @Param("userId") Long userId);

    /**
     * 按用户权限分页查询项目
     */
    List<GitProjectDO> pageProjectByUserId(@Param("req") PageGitProjectReqVO req,
                                           @Param("userId") Long userId,
                                           @Param("limitSize") Integer limitSize,
                                           @Param("pageSize") Integer pageSize);

}