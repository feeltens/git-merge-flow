package com.feeltens.git.controller;

import com.feeltens.git.service.ConflictSessionService;
import com.feeltens.git.vo.base.CloudResponse;
import com.feeltens.git.vo.req.CommitConflictReqVO;
import com.feeltens.git.vo.req.InitConflictReqVO;
import com.feeltens.git.vo.req.ResolveFileReqVO;
import com.feeltens.git.vo.resp.CommitResultVO;
import com.feeltens.git.vo.resp.ConflictDetailVO;
import com.feeltens.git.vo.resp.ConflictFileVO;
import com.feeltens.git.vo.resp.ConflictSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 冲突解决 REST API 控制器
 *
 * @author feeltens
 */
@RestController
@RequestMapping("/api/conflict")
@Slf4j
public class ConflictController {

    @Resource
    private ConflictSessionService conflictSessionService;

    /**
     * 初始化冲突解决会话
     *
     * @param req 初始化请求
     * @return 会话信息
     */
    @PostMapping("/init")
    public CloudResponse<ConflictSessionVO> initConflictSession(@RequestBody InitConflictReqVO req) {
        log.info("初始化冲突解决会话: projectId={}, source={}, target={}",
                req.getProjectId(), req.getSourceBranch(), req.getTargetBranch());
        ConflictSessionVO session = conflictSessionService.initSession(req);
        return CloudResponse.success(session);
    }

    /**
     * 获取冲突文件列表
     *
     * @param sessionId 会话ID
     * @return 冲突文件列表
     */
    @GetMapping("/{sessionId}/files")
    public CloudResponse<List<ConflictFileVO>> getConflictFiles(@PathVariable String sessionId) {
        List<ConflictFileVO> files = conflictSessionService.getConflictFiles(sessionId);
        return CloudResponse.success(files);
    }

    /**
     * 获取单个文件的冲突详情
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return 冲突详情
     */
    @GetMapping("/{sessionId}/file")
    public CloudResponse<ConflictDetailVO> getConflictDetail(@PathVariable String sessionId,
                                                             @RequestParam String filePath) {
        ConflictDetailVO detail = conflictSessionService.getConflictDetail(sessionId, filePath);
        return CloudResponse.success(detail);
    }

    /**
     * 保存单个文件的解决结果
     *
     * @param sessionId 会话ID
     * @param req       解决请求
     * @return 操作结果
     */
    @PostMapping("/{sessionId}/resolve")
    public CloudResponse<Void> resolveFile(@PathVariable String sessionId,
                                           @RequestBody ResolveFileReqVO req) {
        log.info("resolveFile  sessionId:{}    filePath:{}    resolvedContent:{}",
                sessionId, req.getFilePath(), req.getResolvedContent());
        conflictSessionService.resolveFile(sessionId, req);
        return CloudResponse.success();
    }

    /**
     * 提交并推送所有解决的冲突
     *
     * @param sessionId 会话ID
     * @param req       提交请求
     * @return 提交结果
     */
    @PostMapping("/{sessionId}/commit")
    public CloudResponse<CommitResultVO> commitAndPush(@PathVariable String sessionId,
                                                       @RequestBody CommitConflictReqVO req) {
        log.info("提交冲突解决: sessionId={}", sessionId);
        CommitResultVO result = conflictSessionService.commitAndPush(sessionId, req);
        return CloudResponse.success(result);
    }

    /**
     * 取消冲突解决会话
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}")
    public CloudResponse<Void> cancelSession(@PathVariable String sessionId) {
        log.info("取消冲突解决会话: sessionId={}", sessionId);
        conflictSessionService.cancelSession(sessionId);
        return CloudResponse.success();
    }

    /**
     * 重置单个文件到初始冲突状态
     *
     * @param sessionId 会话ID
     * @param filePath  文件路径
     * @return 操作结果
     */
    @PostMapping("/{sessionId}/reset")
    public CloudResponse<Void> resetFile(@PathVariable String sessionId,
                                         @RequestParam String filePath) {
        conflictSessionService.resetFile(sessionId, filePath);
        return CloudResponse.success();
    }

}