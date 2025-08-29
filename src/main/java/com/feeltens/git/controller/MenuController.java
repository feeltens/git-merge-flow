package com.feeltens.git.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * MenuController 菜单 主控制器
 *
 * @author feeltens
 * @date 2025-08-21
 */
@Controller
@Slf4j
public class MenuController {

    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "project";
    }

    /**
     * Git工程管理页面
     */
    @GetMapping("/project")
    public String project() {
        return "project";
    }

    /**
     * Git组织管理页面
     */
    @GetMapping("/organization")
    public String organization() {
        return "organization";
    }

    /**
     * Git分支管理页面
     */
    @GetMapping("/branch")
    public String branch() {
        return "branch";
    }

    /**
     * 中间分支详情页面
     */
    @GetMapping("/mix-branch/detail")
    public String mixBranchDetail() {
        return "mix-branch-detail";
    }

}