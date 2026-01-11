package com.feeltens.git.controller;

import com.feeltens.git.service.UserService;
import com.feeltens.git.vo.base.CloudResponse;
import com.feeltens.git.vo.req.LoginReqVO;
import com.feeltens.git.vo.resp.LoginRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 认证控制器
 *
 * @author feeltens
 * @date 2026-01-11
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public CloudResponse<LoginRespVO> login(@RequestBody LoginReqVO req) {
        try {
            log.info("用户登录, username:{}", req.getUsername());
            LoginRespVO resp = userService.login(req);
            log.info("用户登录成功, username:{}, userId:{}", req.getUsername(), resp.getUserId());
            return CloudResponse.success(resp);
        } catch (Exception e) {
            log.error("用户登录失败, username:{}, error:{}", req.getUsername(), e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public CloudResponse<String> logout() {
        try {
            userService.logout();
            return CloudResponse.success("登出成功");
        } catch (Exception e) {
            log.error("用户登出失败, error:{}", e.getMessage());
            return CloudResponse.fail(e.getMessage());
        }
    }

}