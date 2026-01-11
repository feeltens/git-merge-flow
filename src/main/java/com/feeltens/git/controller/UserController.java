package com.feeltens.git.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.alibaba.fastjson.JSON;
import com.feeltens.git.service.UserService;
import com.feeltens.git.vo.base.CloudResponse;
import com.feeltens.git.vo.base.PageRequest;
import com.feeltens.git.vo.base.PageResponse;
import com.feeltens.git.vo.req.AddUserReqVO;
import com.feeltens.git.vo.req.DeleteUserReqVO;
import com.feeltens.git.vo.req.PageUserReqVO;
import com.feeltens.git.vo.req.UpdatePasswordReqVO;
import com.feeltens.git.vo.req.UpdateUserProjectPermReqVO;
import com.feeltens.git.vo.req.UpdateUserReqVO;
import com.feeltens.git.vo.resp.ProjectSimpleVO;
import com.feeltens.git.vo.resp.UserInfoRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户管理控制器
 *
 * @author feeltens
 * @date 2026-01-11
 */
@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/currentUser")
    public CloudResponse<UserInfoRespVO> getCurrentUser() {
        try {
            UserInfoRespVO resp = userService.getCurrentUserInfo();
            return CloudResponse.success(resp);
        } catch (Exception e) {
            log.error("获取当前用户信息失败, error:{}", e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 分页查询用户列表 (管理员)
     */
    @PostMapping("/pageUser")
    @SaCheckRole("ADMIN")
    public CloudResponse<PageResponse<UserInfoRespVO>> pageUser(@RequestBody PageRequest<PageUserReqVO> req) {
        try {
            PageResponse<UserInfoRespVO> resp = userService.pageUser(req);
            return CloudResponse.success(resp);
        } catch (Exception e) {
            log.error("分页查询用户失败, param:{}, error:{}", JSON.toJSONString(req), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 新增用户 (管理员)
     */
    @PostMapping("/addUser")
    @SaCheckRole("ADMIN")
    public CloudResponse<String> addUser(@RequestBody AddUserReqVO req) {
        try {
            log.info("新增用户, param:{}", JSON.toJSONString(req));
            userService.addUser(req);
            return CloudResponse.success("新增成功");
        } catch (Exception e) {
            log.error("新增用户失败, param:{}, error:{}", JSON.toJSONString(req), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 更新用户 (管理员)
     */
    @PostMapping("/updateUser")
    @SaCheckRole("ADMIN")
    public CloudResponse<String> updateUser(@RequestBody UpdateUserReqVO req) {
        try {
            log.info("更新用户, param:{}", JSON.toJSONString(req));
            userService.updateUser(req);
            return CloudResponse.success("更新成功");
        } catch (Exception e) {
            log.error("更新用户失败, param:{}, error:{}", JSON.toJSONString(req), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 删除用户 (管理员)
     */
    @PostMapping("/deleteUser")
    @SaCheckRole("ADMIN")
    public CloudResponse<String> deleteUser(@RequestBody DeleteUserReqVO req) {
        try {
            log.info("删除用户, userId:{}, operator:{}", req.getUserId(), req.getOperator());
            userService.deleteUser(req.getUserId(), req.getOperator());
            return CloudResponse.success("删除成功");
        } catch (Exception e) {
            log.error("删除用户失败, userId:{}, error:{}", req.getUserId(), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 修改自己的密码 (所有用户)
     */
    @PostMapping("/updateMyPassword")
    public CloudResponse<String> updateMyPassword(@RequestBody UpdatePasswordReqVO req) {
        try {
            userService.updateMyPassword(req);
            return CloudResponse.success("密码修改成功");
        } catch (Exception e) {
            log.error("修改密码失败, error:{}", e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 重置用户密码 (管理员)
     */
    @PostMapping("/resetPassword")
    @SaCheckRole("ADMIN")
    public CloudResponse<String> resetPassword(@RequestBody UpdatePasswordReqVO req) {
        try {
            log.info("重置用户密码, userId:{}, operator:{}", req.getUserId(), req.getOperator());
            userService.resetPassword(req);
            return CloudResponse.success("密码重置成功");
        } catch (Exception e) {
            log.error("重置密码失败, userId:{}, error:{}", req.getUserId(), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 更新用户工程权限 (管理员)
     */
    @PostMapping("/updateUserProjectPerm")
    @SaCheckRole("ADMIN")
    public CloudResponse<String> updateUserProjectPerm(@RequestBody UpdateUserProjectPermReqVO req) {
        try {
            log.info("更新用户工程权限, param:{}", JSON.toJSONString(req));
            userService.updateUserProjectPerm(req);
            return CloudResponse.success("权限更新成功");
        } catch (Exception e) {
            log.error("更新用户工程权限失败, param:{}, error:{}", JSON.toJSONString(req), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取所有项目列表（用于权限分配）
     */
    @GetMapping("/allProjects")
    @SaCheckRole("ADMIN")
    public CloudResponse<List<ProjectSimpleVO>> getAllProjects() {
        try {
            List<ProjectSimpleVO> list = userService.getAllProjects();
            return CloudResponse.success(list);
        } catch (Exception e) {
            log.error("获取所有项目列表失败, error:{}", e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

}