package com.feeltens.git.mapper;

import com.feeltens.git.entity.GitBranchDO;
import com.feeltens.git.vo.req.ListGitBranchReqVO;
import com.feeltens.git.vo.req.PageGitBranchReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GitBranchMapper {

    int insertList(@Param("list") List<GitBranchDO> list);

    Long countBranch(@Param("req") PageGitBranchReqVO item);

    List<GitBranchDO> pageBranch(@Param("req") PageGitBranchReqVO item,
                                 @Param("limitSize") Integer limitSize,
                                 @Param("pageSize") Integer pageSize);

    GitBranchDO queryByBranchName(@Param("gitBranchName") String gitBranchName);

    List<GitBranchDO> queryByProjectId(@Param("projectId") Long projectId);

    int insertOrUpdateByBranchName(@Param("item") GitBranchDO gitBranch);

    int deleteByProjectIdAndBranchNameList(@Param("projectId") Long projectId,
                                           @Param("branchNameList") List<String> branchNameList,
                                           @Param("operator") String operator);

    int deleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据项目id和分支名称查询
     */
    List<GitBranchDO> queryByProjectIdAndBranchName(@Param("req") ListGitBranchReqVO req);

}