-- 创建数据库
CREATE DATABASE IF NOT EXISTS `git_merge_flow` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `git_merge_flow`;

-- git组织
CREATE TABLE `git_organization` (
  `org_inner_id` bigint NOT NULL AUTO_INCREMENT COMMENT '组织自增id',
  `organization_id` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '组织id(git服务平台的组织id)',
  `organization_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '组织名称',
  `description` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织描述',
  `web_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'webUrl',
  `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除标识: 0代表未删除，>0代表已删除，此时同org_inner_id',
  PRIMARY KEY (`org_inner_id`),
  UNIQUE KEY `uk_index` (`organization_id`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='git组织';

-- git工程
CREATE TABLE `git_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工程id',
  `project_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工程名称',
  `repository_url` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库地址',
  `organization_id` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组织id',
  `repository_id` bigint NOT NULL COMMENT '仓库id',
  `project_username` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工程仓库用户名',
  `project_email` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '工程仓库邮箱',
  `default_branch` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '代码库默认分支',
  `web_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'webUrl',
  `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除标识: 0代表未删除，>0代表已删除，此时同project_id',
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `uk_index` (`project_name`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='git工程';

-- git原始分支
CREATE TABLE `git_branch` (
  `branch_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分支id',
  `branch_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分支名称',
  `project_id` bigint NOT NULL COMMENT '工程id',
  `branch_desc` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分支备注',
  `default_branch_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是默认分支 默认为0-非默认分支；1-默认分支',
  `merged_master_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '已合并进master状态 默认为0-未合并进master；1-已合并进master',
  `branch_ext` json DEFAULT NULL COMMENT '分支ext扩展信息',
  `source_branch` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建来源分支名称',
  `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `last_commit_time` datetime NOT NULL COMMENT '最近一次提交时间',
  `last_commit_user` varchar(100) NOT NULL COMMENT '最近一次提交人',
  `last_commit_email` varchar(100) NOT NULL COMMENT '最近一次提交人邮箱',
  `last_commit_id` varchar(100) NOT NULL COMMENT '最近一次提交ID',
  `last_commit_short_id` varchar(100) NOT NULL COMMENT '最近一次提交shortID',
  `last_commit_message` text NOT NULL COMMENT '最近一次提交内容',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除标识: 0代表未删除，>0代表已删除，此时同branch_id',
  PRIMARY KEY (`branch_id`),
  UNIQUE KEY `uk_index` (`project_id`,`branch_name`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='git原始分支';

-- git中间分支
CREATE TABLE `git_mix_branch` (
  `mix_branch_id` bigint NOT NULL AUTO_INCREMENT COMMENT '中间分支id',
  `mix_branch_name` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '中间分支名称',
  `env` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '环境：dev开发环境；test测试环境；pre预发环境',
  `project_id` bigint NOT NULL COMMENT '工程id',
  `all_merge_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '合并总状态 默认为0-未成功；1-成功',
  `merge_request_id` bigint DEFAULT NULL COMMENT 'git服务平台的合并请求id',
  `mix_branch_ext` json DEFAULT NULL COMMENT '中间分支ext扩展信息',
  `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除标识: 0代表未删除，>0代表已删除，此时同mix_branch_id',
  PRIMARY KEY (`mix_branch_id`),
  UNIQUE KEY `uk_index` (`mix_branch_name`,`env`,`project_id`,`deleted`) USING BTREE,
   KEY `idx_query` (`project_id`,`env`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='git中间分支';

-- git中间分支条目
CREATE TABLE `git_mix_branch_item` (
  `mix_branch_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `mix_branch_id` bigint NOT NULL COMMENT '中间分支id',
  `branch_name` varchar(100) NOT NULL COMMENT '原始分支名称',
  `merge_flag` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否合并的标识，0代表未合并，1代表已合并',
  `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除标识: 0代表未删除，>0代表已删除，此时同mix_branch_item_id',
  PRIMARY KEY (`mix_branch_item_id`),
  UNIQUE KEY `uk_index` (`mix_branch_id`,`branch_name`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='git中间分支的条目';

-- =============================================
-- 用户权限相关表
-- =============================================

-- 系统用户表
CREATE TABLE `sys_user` (
                            `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
                            `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
                            `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
                            `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
                            `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '更新人',
                            `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted` bigint NOT NULL DEFAULT '0' COMMENT '删除标识: 0代表未删除，>0代表已删除',
                            PRIMARY KEY (`user_id`),
                            UNIQUE KEY `uk_username` (`username`, `deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 用户角色关联表
CREATE TABLE `sys_user_role` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                 `user_id` bigint NOT NULL COMMENT '用户ID',
                                 `role_code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码: ADMIN-管理员, USER-普通用户',
                                 `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `uk_user_role` (`user_id`, `role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 用户Project权限表
CREATE TABLE `sys_user_project_perm` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                         `user_id` bigint NOT NULL COMMENT '用户ID',
                                         `project_id` bigint NOT NULL COMMENT '工程ID',
                                         `perm_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'READ_WRITE' COMMENT '权限类型: READ-只读, READ_WRITE-读写',
                                         `create_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'SYSTEM' COMMENT '创建人',
                                         `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `uk_user_project` (`user_id`, `project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Project权限表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化管理员账号 (密码: admin123)
-- 使用BCrypt加密，可通过 PasswordUtil.encode("admin123") 生成
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`)
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 1);

-- 设置管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_code`) VALUES (1, 'ADMIN');
