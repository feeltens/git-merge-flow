package com.feeltens.git.mapper;

import com.feeltens.git.entity.SysUserDO;
import com.feeltens.git.vo.req.PageUserReqVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统用户Mapper
 *
 * @author feeltens
 * @date 2026-01-11
 */
public interface SysUserMapper {

    /**
     * 根据用户名查询用户
     */
    SysUserDO selectByUsername(@Param("username") String username);

    /**
     * 根据昵称查询用户
     */
    SysUserDO selectByNickname(@Param("nickname") String nickname);

    /**
     * 根据用户ID查询用户
     */
    SysUserDO selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询用户列表
     */
    List<SysUserDO> selectPageList(@Param("param") PageUserReqVO param,
                                   @Param("offset") Integer offset,
                                   @Param("pageSize") Integer pageSize);

    /**
     * 查询用户总数
     */
    Long selectCount(@Param("param") PageUserReqVO param);

    /**
     * 新增用户
     */
    int insert(SysUserDO user);

    /**
     * 更新用户
     */
    int updateById(SysUserDO user);

    /**
     * 更新密码
     */
    int updatePassword(@Param("userId") Long userId,
                       @Param("password") String password,
                       @Param("updateBy") String updateBy);

    /**
     * 逻辑删除用户
     */
    int deleteById(@Param("userId") Long userId,
                   @Param("updateBy") String updateBy);

}