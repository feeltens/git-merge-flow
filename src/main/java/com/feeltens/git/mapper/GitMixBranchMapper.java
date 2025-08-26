package com.feeltens.git.mapper;

import com.feeltens.git.entity.GitMixBranchDO;
import com.feeltens.git.vo.req.PageMixBranchReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GitMixBranchMapper {

    int insertList(@Param("list") List<GitMixBranchDO> list);

    Long countMixBranch(@Param("req") PageMixBranchReqVO req);

    List<GitMixBranchDO> pageMixBranch(@Param("req") PageMixBranchReqVO item,
                                       @Param("limitSize") Integer limitSize,
                                       @Param("pageSize") Integer pageSize);

    GitMixBranchDO queryByMixBranchId(@Param("mixBranchId") Long mixBranchId);

    GitMixBranchDO queryByProjectIdAndEnv(@Param("projectId") Long projectId,
                                          @Param("env") String env);

    int updateAllMergeFlag(@Param("mixBranchId") Long mixBranchId,
                           @Param("allMergeFlag") Integer allMergeFlag,
                           @Param("operator") String operator);

    int updateMergeRequestId(@Param("mixBranchId") Long mixBranchId,
                             @Param("mergeRequestId") Long mergeRequestId);

    int updateExt(@Param("mixBranchId") Long mixBranchId,
                  @Param("ext") String ext);

    int deleteByMixBranchId(@Param("mixBranchId") Long mixBranchId);

    int deleteByProjectId(@Param("projectId") Long projectId);

}