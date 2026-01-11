package com.feeltens.git.service;

import com.feeltens.git.vo.base.PageRequest;
import com.feeltens.git.vo.base.PageResponse;
import com.feeltens.git.vo.req.AddUserReqVO;
import com.feeltens.git.vo.req.LoginReqVO;
import com.feeltens.git.vo.req.PageUserReqVO;
import com.feeltens.git.vo.req.UpdatePasswordReqVO;
import com.feeltens.git.vo.req.UpdateUserProjectPermReqVO;
import com.feeltens.git.vo.req.UpdateUserReqVO;
import com.feeltens.git.vo.resp.LoginRespVO;
import com.feeltens.git.vo.resp.ProjectSimpleVO;
import com.feeltens.git.vo.resp.UserInfoRespVO;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author feeltens
 * @date 2026-01-11
 */
public interface UserService {

    /**
     * 用户登录
     */
    LoginRespVO login(LoginReqVO req);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 获取当前登录用户信息
     */
    UserInfoRespVO getCurrentUserInfo();

    /**
     * 分页查询用户列表
     */
    PageResponse<UserInfoRespVO> pageUser(PageRequest<PageUserReqVO> req);

    /**
     * 新增用户
     */
    void addUser(AddUserReqVO req);

    /**
     * 更新用户
     */
    void updateUser(UpdateUserReqVO req);

    /**
     * 删除用户
     */
    void deleteUser(Long userId, String operator);

    /**
     * 修改自己的密码
     */
    void updateMyPassword(UpdatePasswordReqVO req);

    /**
     * 管理员重置用户密码
     */
    void resetPassword(UpdatePasswordReqVO req);

    /**
     * 更新用户Project权限
     */
    void updateUserProjectPerm(UpdateUserProjectPermReqVO req);

    /**
     * 检查用户是否是管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 获取所有项目列表（用于权限分配）
     */
    List<ProjectSimpleVO> getAllProjects();

}