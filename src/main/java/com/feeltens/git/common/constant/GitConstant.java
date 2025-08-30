package com.feeltens.git.common.constant;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * 常量
 *
 * @author feeltens
 * @date 2025-08-20
 */
public class GitConstant {

    /**
     * git中间分支名 后缀
     * <pre>
     *     dev环境 dev_mix
     *     test环境 test_mix
     *     pre环境 pre_mix
     * </pre>
     */
    public static final String MIX_BRANCH_NAME_SUFFIX = "_mix";

    /**
     * 系统操作人名称
     */
    public static final String SYSTEM_NAME = "GitMergeFlow";

    /**
     * 排除的git中间分支名列表
     * todo 暂时先写死
     */
    public static final List<String> EXCLUDE_MIX_BRANCH_NAME_LIST = Lists.newArrayList("dev_mix", "test_mix", "pre_mix");

    /**
     * 备份的git中间分支名 片段
     */
    public static final String BACKUP_MIX_BRANCH_NAME_LIKE = "gitbackup_xim";

}