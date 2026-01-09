package com.feeltens.git.service;

import com.feeltens.git.dto.conflict.ConflictSession;
import com.feeltens.git.vo.req.CommitConflictReqVO;
import com.feeltens.git.vo.req.InitConflictReqVO;
import com.feeltens.git.vo.req.ResolveFileReqVO;
import com.feeltens.git.vo.resp.CommitResultVO;
import com.feeltens.git.vo.resp.ConflictDetailVO;
import com.feeltens.git.vo.resp.ConflictFileVO;
import com.feeltens.git.vo.resp.ConflictSessionVO;

import java.util.List;

/**
 * 冲突解决会话服务接口
 *
 * @author feeltens
 */
public interface ConflictSessionService {

    /**
     * 初始化冲突解决会话
     *
     * @param req 初始化请求
     * @return 会话信息
     */
    ConflictSessionVO initSession(InitConflictReqVO req);

    /**
     * 获取会话信息
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    ConflictSession getSession(String sessionId);

    /**
     * 获取冲突文件列表
     *
     * @param sessionId 会话ID
     * @return 冲突文件列表
     */
    List<ConflictFileVO> getConflictFiles(String sessionId);

    /**
     * 获取文件冲突详情
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return 冲突详情
     */
    ConflictDetailVO getConflictDetail(String sessionId, String filePath);

    /**
     * 保存文件解决结果
     *
     * @param sessionId 会话ID
     * @param req       解决请求
     */
    void resolveFile(String sessionId, ResolveFileReqVO req);

    /**
     * 提交并推送
     *
     * @param sessionId 会话ID
     * @param req       提交请求
     * @return 提交结果
     */
    CommitResultVO commitAndPush(String sessionId, CommitConflictReqVO req);

    /**
     * 取消会话
     *
     * @param sessionId 会话ID
     */
    void cancelSession(String sessionId);

    /**
     * 重置文件到初始冲突状态
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     */
    void resetFile(String sessionId, String filePath);

    /**
     * 清理过期会话
     */
    void cleanupExpiredSessions();

    /**
     * 清理指定项目的所有会话
     *
     * @param projectName 项目名称
     */
    void clearSessionsByProjectName(String projectName);

}