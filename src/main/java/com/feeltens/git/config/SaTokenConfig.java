package com.feeltens.git.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 *
 * @author feeltens
 * @date 2026-01-11
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验 -- 排除登录接口、静态资源、登录页面
            SaRouter.match("/**")
                    .notMatch(
                            "/auth/login",
                            "/auth/logout",
                            "/login",
                            "/static/**",
                            "/css/**",
                            "/js/**",
                            "/img/**",
                            "/favicon.ico",
                            "/favicon-*.png",
                            "/error"
                    )
                    .check(r -> StpUtil.checkLogin());

            // 用户管理接口 - 仅管理员可访问 (除了修改自己密码)
            SaRouter.match("/api/v1/user/**")
                    .notMatch("/api/v1/user/updateMyPassword", "/api/v1/user/currentUser")
                    .check(r -> StpUtil.checkRole("ADMIN"));

        })).addPathPatterns("/**");
    }

}