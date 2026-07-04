package com.feeltens.git.service;

import com.feeltens.git.dto.diff.ThreeWayDiffVO;

/**
 * Diff渲染服务接口
 *
 * @author feeltens
 */
public interface DiffRenderService {

    /**
     * 计算三路diff
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return 三路diff视图对象
     */
    ThreeWayDiffVO calculateThreeWayDiff(String sessionId, String filePath);

}