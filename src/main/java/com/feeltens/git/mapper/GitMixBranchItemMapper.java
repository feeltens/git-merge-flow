package com.feeltens.git.mapper;

import com.feeltens.git.entity.GitMixBranchItemDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GitMixBranchItemMapper {

    int insertList(@Param("list") List<GitMixBranchItemDO> list);

    int insertOrUpdateByBranchName(@Param("item") GitMixBranchItemDO gitMixBranchItem);

    List<GitMixBranchItemDO> queryByMixBranchId(@Param("mixBranchId") Long mixBranchId);

    int deleteByMixBranchIdAndBranchNameList(@Param("mixBranchId") Long mixBranchId,
                                             @Param("branchNameList") List<String> branchNameList);

    int deleteByMixBranchId(@Param("mixBranchId") Long mixBranchId);

    int deleteByMixBranchIds(@Param("mixBranchIdList") List<Long> mixBranchIdList);

    int updateMergeFlag(@Param("mixBranchId") Long mixBranchId,
                        @Param("branchName") String branchName,
                        @Param("mergeFlag") Integer mergeFlag,
                        @Param("operator") String operator);

}