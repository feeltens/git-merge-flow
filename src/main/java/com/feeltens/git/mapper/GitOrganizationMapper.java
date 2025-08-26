package com.feeltens.git.mapper;

import com.feeltens.git.entity.GitOrganizationDO;
import com.feeltens.git.vo.req.PageGitOrganizationReqVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GitOrganizationMapper {

    int insert(GitOrganizationDO gitOrganizationDO);

    GitOrganizationDO queryByOrganizationId(@Param("organizationId") String organizationId);

    Long countOrganization(@Param("req") PageGitOrganizationReqVO item);

    List<GitOrganizationDO> pageOrganization(@Param("req") PageGitOrganizationReqVO item,
                                             @Param("limitSize") Integer limitSize,
                                             @Param("pageSize") Integer pageSize);

    List<GitOrganizationDO> listOrganization();

}