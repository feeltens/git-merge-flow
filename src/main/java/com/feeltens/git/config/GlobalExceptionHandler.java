package com.feeltens.git.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.feeltens.git.common.exception.BizException;
import com.feeltens.git.vo.base.CloudResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局异常处理
 *
 * @author feeltens
 * @date 2026-01-11
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 判断是否为API请求
     * 通过以下方式判断：
     * 1. URI以 /api/ 或 /auth/ 开头
     * 2. X-Requested-With 头为 XMLHttpRequest (Ajax请求)
     * 3. Accept 头明确只接受 application/json（不包含 text/html）
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // API路径判断
        if (uri.startsWith("/api/") || uri.startsWith("/auth/")) {
            return true;
        }

        // Ajax请求判断
        String xRequestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equalsIgnoreCase(xRequestedWith)) {
            return true;
        }
        // Accept头判断 - 只有明确接受JSON且不接受HTML时才认为是API请求
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json") && !accept.contains("text/html")) {
            return true;
        }
        return false;
    }

    /**
     * 处理未登录异常
     * - API请求：返回401 JSON响应
     * - 页面请求：重定向到登录页
     */
    @ExceptionHandler(NotLoginException.class)
    public CloudResponse<String> handleNotLoginException(NotLoginException e,
                                                         HttpServletRequest request,
                                                         HttpServletResponse response) {
        log.warn("用户未登录: {}", e.getMessage());

        if (isApiRequest(request)) {
            // API请求返回JSON
            response.setStatus(401);
            return CloudResponse.fail("请先登录");
        } else {
            // 页面请求重定向到登录页
            try {
                response.sendRedirect("/login");
            } catch (IOException ex) {
                log.error("重定向失败", ex);
            }
            return null;
        }
    }

    /**
     * 处理无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public CloudResponse<String> handleNotRoleException(NotRoleException e, HttpServletResponse response) {
        log.warn("用户无权限: {}", e.getMessage());
        response.setStatus(403);
        return CloudResponse.fail("无权限访问");
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    public CloudResponse<String> handleBizException(BizException e) {
        log.error("业务异常: {}", e.getMessage());
        return CloudResponse.fail(e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public CloudResponse<String> handleException(Exception e) {
        log.error("系统异常: ", e);
        return CloudResponse.fail("系统异常，请稍后重试");
    }

}